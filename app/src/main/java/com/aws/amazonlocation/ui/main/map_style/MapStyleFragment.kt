package com.aws.amazonlocation.ui.main.map_style // ktlint-disable package-name

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.aws.amazonlocation.R
import com.aws.amazonlocation.databinding.FragmentMapStyleBinding
import com.aws.amazonlocation.ui.base.BaseFragment
import com.aws.amazonlocation.ui.main.MainActivity
import com.aws.amazonlocation.utils.KEY_MAP_NAME
import com.aws.amazonlocation.utils.KEY_MAP_STYLE_NAME
import com.aws.amazonlocation.utils.isInternetAvailable
import kotlin.math.ceil

class MapStyleFragment : BaseFragment() {

    private lateinit var mLayoutManagerEsri: GridLayoutManager
    private lateinit var mLayoutManagerHere: GridLayoutManager
    private lateinit var mBinding: FragmentMapStyleBinding
    private val mViewModel: MapStyleViewModel by viewModels()
    private var mAdapter: EsriMapStyleAdapter? = null
    private var mHereAdapter: EsriMapStyleAdapter? = null
    private var mGrabAdapter: EsriMapStyleAdapter? = null
    private var isMapClickEnable = true
    private var isTablet = false
    private var isLargeTablet = false
    private var columnCount = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMapStyleBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if ((activity is MainActivity)) {
            isTablet = (activity as MainActivity).isTablet
        }
        isLargeTablet = requireContext().resources.getBoolean(R.bool.is_large_tablet)
        if (isTablet) {
            setColumnCount()
        }
        val mapStyle =
            mPreferenceManager.getValue(KEY_MAP_STYLE_NAME, getString(R.string.map_light))
        val mapName = mPreferenceManager.getValue(KEY_MAP_NAME, getString(R.string.map_esri))

        mViewModel.setEsriMapListData(requireContext())
        mViewModel.setHereMapListData(requireContext())
        mViewModel.setGrabMapListData(requireContext())
        when (mapName) {
            resources.getString(R.string.esri) -> {
                for (i in 0 until mViewModel.esriList.size) {
                    mViewModel.esriList[i].isSelected = mViewModel.esriList[i].mapName == mapStyle
                }
            }
            resources.getString(R.string.here) -> {
                for (i in 0 until mViewModel.hereList.size) {
                    mViewModel.hereList[i].isSelected = mViewModel.hereList[i].mapName == mapStyle
                }
            }
            resources.getString(R.string.grab) -> {
                for (i in 0 until mViewModel.grabList.size) {
                    mViewModel.grabList[i].isSelected = mViewModel.grabList[i].mapName == mapStyle
                }
            }
        }

        setEsriMapStyleAdapter()
        setHereMapStyleAdapter()
        setGrabMapStyleAdapter()
        backPress()
        clickListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        setColumnCount()
        mLayoutManagerHere.spanCount = columnCount
        mLayoutManagerEsri.spanCount = columnCount
        mHereAdapter?.notifyDataSetChanged()
        mAdapter?.notifyDataSetChanged()
    }

    private fun setColumnCount() {
        columnCount = calculateColumnCount()
    }

    private fun calculateColumnCount(): Int {
        val calculatedColumn: Double = (requireContext().resources.displayMetrics.widthPixels).toDouble() / 650
        return ceil(calculatedColumn).toInt()
    }

    private fun setHereMapStyleAdapter() {
        mLayoutManagerHere = GridLayoutManager(this.context, columnCount)
        mBinding.apply {
            rvHere.layoutManager = mLayoutManagerHere
            mHereAdapter = EsriMapStyleAdapter(
                mViewModel.hereList,
                object : EsriMapStyleAdapter.EsriMapStyleInterface {
                    override fun esriStyleClick(position: Int) {
                        if (context?.isInternetAvailable() == true) {
                            if (isMapClickEnable) {
                                isMapClickEnable = false
                                changeStyle(position, isHere = true, isGrab = false)
                            }
                        } else {
                            showError(getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
                }
            )
            rvHere.adapter = mHereAdapter
        }
    }

    private fun setEsriMapStyleAdapter() {
        mLayoutManagerEsri = GridLayoutManager(this.context, columnCount)
        mBinding.apply {
            rvEsri.layoutManager = mLayoutManagerEsri
            mAdapter = EsriMapStyleAdapter(
                mViewModel.esriList,
                object : EsriMapStyleAdapter.EsriMapStyleInterface {
                    override fun esriStyleClick(position: Int) {
                        if (context?.isInternetAvailable() == true) {
                            if (isMapClickEnable) {
                                isMapClickEnable = false
                                changeStyle(position, isHere = false, isGrab = false)
                            }
                        } else {
                            showError(getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
                }
            )
            rvEsri.adapter = mAdapter
        }
    }

    private fun setGrabMapStyleAdapter() {
        val mLayoutManager = GridLayoutManager(this.context, 3)
        mBinding.apply {
            rvGrab.layoutManager = mLayoutManager
            mGrabAdapter = EsriMapStyleAdapter(
                mViewModel.grabList,
                object : EsriMapStyleAdapter.EsriMapStyleInterface {
                    override fun esriStyleClick(position: Int) {
                        if (context?.isInternetAvailable() == true) {
                            changeStyle(position, isHere = false, isGrab = true)
                        } else {
                            showError(getString(R.string.check_your_internet_connection_and_try_again))
                        }
                    }
                }
            )
            rvGrab.adapter = mGrabAdapter
        }
    }

    fun changeStyle(position: Int, isHere: Boolean, isGrab: Boolean) {
        if (position != -1) {
            if (isGrab) {
                mAdapter?.deselectAll()
                mHereAdapter?.deselectAll()
                mGrabAdapter?.singeSelection(position)
                mViewModel.grabList[position].mapName?.let { it1 ->
                    mPreferenceManager.setValue(
                        KEY_MAP_STYLE_NAME,
                        it1
                    )
                }
                mMapHelper.updateMapStyle(
                    mViewModel.grabList[position].mMapName!!,
                    mViewModel.grabList[position].mMapStyleName!!
                )
                mPreferenceManager.setValue(
                    KEY_MAP_NAME,
                    resources.getString(R.string.grab)
                )
                return
            }
            if (isHere) {
                mAdapter?.deselectAll()
                mGrabAdapter?.deselectAll()
                mHereAdapter?.singeSelection(position)
                mViewModel.hereList[position].mapName?.let { it1 ->
                    mPreferenceManager.setValue(
                        KEY_MAP_STYLE_NAME,
                        it1
                    )
                }
                mViewModel.hereList[position].mMapName?.let {
                    mViewModel.hereList[position].mMapStyleName?.let { it1 ->
                        mMapHelper.updateMapStyle(
                            it,
                            it1
                        )
                    }
                }
                mPreferenceManager.setValue(
                    KEY_MAP_NAME,
                    resources.getString(R.string.here)
                )
            } else {
                mHereAdapter?.deselectAll()
                mGrabAdapter?.deselectAll()
                mAdapter?.singeSelection(position)
                mViewModel.esriList[position].mapName?.let { it1 ->
                    mPreferenceManager.setValue(
                        KEY_MAP_STYLE_NAME,
                        it1
                    )
                }
                mPreferenceManager.setValue(
                    KEY_MAP_NAME,
                    resources.getString(R.string.map_esri)
                )
                mViewModel.esriList[position].mMapName?.let {
                    mViewModel.esriList[position].mMapStyleName?.let { it1 ->
                        mMapHelper.updateMapStyle(
                            it,
                            it1
                        )
                    }
                }
            }
        }
        isMapClickEnable = true
    }

    private fun clickListener() {
        mBinding.ivMapStyleBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }
}
