package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_NO_TRACKING_HISTORY
import com.aws.amazonlocation.TEST_FAILED_NO_UPDATE_TRACKING_HISTORY
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingStopTrackingTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showStopTrackingTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_1000)

            val tracking = uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.menu_tracking)))
            tracking.click()
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker))),
                DELAY_1000
            )
            val labelContinue =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracker_continue_to_tracker)))
            labelContinue?.click()

            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_enable_tracking))),
                DELAY_1000
            )

            val clEnableTracking =
                mActivityRule.activity.findViewById<ConstraintLayout>(R.id.cl_enable_tracking)
            if (clEnableTracking.visibility == View.VISIBLE) {
                val btnEnableTracking =
                    mActivityRule.activity.findViewById<MaterialCardView>(R.id.btn_enable_tracking)
                mActivityRule.activity.runOnUiThread {
                    btnEnableTracking.performClick()
                }
            }
            Thread.sleep(DELAY_5000)
            val rvTracking =
                mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_tracking)
            var itemCount = rvTracking.adapter?.itemCount ?: 0
            uiDevice.wait(
                Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking))),
                DELAY_1000
            )
            val labelStartTracking =
                uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking)))
            labelStartTracking?.click()

            Thread.sleep(DELAY_20000)
            if (rvTracking.adapter?.itemCount != null) {
                rvTracking.adapter?.itemCount?.let {
                    if (itemCount <= it) {
                        uiDevice.wait(
                            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_stop_tracking))),
                            DELAY_1000
                        )
                        val labelStopTracking =
                            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_stop_tracking)))
                        labelStopTracking?.click()
                        rvTracking.adapter?.itemCount?.let { it1 ->
                            itemCount = it1
                        }
                        Thread.sleep(DELAY_10000)
                        rvTracking.adapter?.itemCount?.let { it1 ->
                            Assert.assertTrue(TEST_FAILED_NO_UPDATE_TRACKING_HISTORY, itemCount == it1)
                        }
                    } else {
                        Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
                    }
                }
            } else {
                Assert.fail(TEST_FAILED_NO_TRACKING_HISTORY)
            }
        } catch (_: Exception) {
            Assert.fail(TEST_FAILED)
        }
    }
}
