package com.aws.amazonlocation.ui.main

import android.location.Location
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.DELAY_1000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_3000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.SECOND_DELAY_60
import com.aws.amazonlocation.TRACKING_ENTERED
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.mockLocations
import com.google.android.material.card.MaterialCardView
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class TrackingGeofenceEnterTest {

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
    fun showTrackingGeofenceEnterTest() {
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
        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking))),
            DELAY_1000
        )
        val labelStartTracking =
            uiDevice.findObject(By.text(mActivityRule.activity.getString(R.string.label_start_tracking)))
        labelStartTracking?.click()
        var mapbox: MapboxMap? = null
        val mapView = mActivityRule.activity.findViewById<MapLibreView>(R.id.mapView)
        mapView.getMapAsync {
            mapbox = it
        }

        for (mockLocation in mockLocations) {
            mapbox?.let {
                mActivityRule.activity.runOnUiThread {
                    val locationComponent: LocationComponent = it.locationComponent
                    locationComponent.isLocationComponentEnabled = true

                    val lastKnownLocation = Location("gps")
                    lastKnownLocation.latitude = mockLocation.latitude
                    lastKnownLocation.longitude = mockLocation.longitude
                    lastKnownLocation.time = System.currentTimeMillis()
                    lastKnownLocation.accuracy = 1.0f
                    it.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                lastKnownLocation.latitude,
                                lastKnownLocation.longitude
                            ),
                            14.0
                        )
                    )

                    it.locationComponent.forceLocationUpdate(lastKnownLocation)
                }
            }
            runBlocking {
                delay(DELAY_3000) // Sleep for the specified delay time
            }
            val latLng = LatLng(
                mockLocation.latitude,
                mockLocation.longitude
            )
            (mActivityRule.activity as MainActivity).mTrackingUtils?.updateLatLngOnMap(
                latLng
            )
        }
        uiDevice.wait(
            Until.hasObject(By.text(mActivityRule.activity.getString(R.string.ok))),
            SECOND_DELAY_60
        )
        val dialogText = getAlertDialogMessage()
        Assert.assertTrue(dialogText.contains(TRACKING_ENTERED))
    }

    private fun getAlertDialogMessage(): String {
        val appCompatTextViewMatcher = withId(android.R.id.message)
        var messageText = ""
        onView(appCompatTextViewMatcher).perform(object : ViewAction {
            override fun getDescription(): String {
                return "get AlertDialog message"
            }

            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
            }

            override fun perform(uiController: UiController?, view: View?) {
                val textView = view as TextView?
                messageText = textView?.text.toString()
            }
        })
        return messageText
    }
}
