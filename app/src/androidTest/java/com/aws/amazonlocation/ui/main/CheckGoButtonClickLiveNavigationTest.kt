package com.aws.amazonlocation.ui.main

import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.* // ktlint-disable no-wildcard-imports
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.aws.amazonlocation.ACCESS_COARSE_LOCATION
import com.aws.amazonlocation.ACCESS_FINE_LOCATION
import com.aws.amazonlocation.AMAZON_MAP_READY
import com.aws.amazonlocation.BaseTest
import com.aws.amazonlocation.BuildConfig
import com.aws.amazonlocation.DELAY_10000
import com.aws.amazonlocation.DELAY_15000
import com.aws.amazonlocation.DELAY_2000
import com.aws.amazonlocation.DELAY_20000
import com.aws.amazonlocation.DELAY_5000
import com.aws.amazonlocation.R
import com.aws.amazonlocation.TEST_FAILED
import com.aws.amazonlocation.TEST_FAILED_CARD_DRIVE_GO
import com.aws.amazonlocation.TEST_FAILED_DIRECTION_CARD
import com.aws.amazonlocation.TEST_FAILED_EXIT_BUTTON_NOT_VISIBLE
import com.aws.amazonlocation.TEST_FAILED_SEARCH_DIRECTION
import com.aws.amazonlocation.TEST_WORD_4
import com.aws.amazonlocation.di.AppModule
import com.aws.amazonlocation.enableGPS
import com.aws.amazonlocation.failTest
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@UninstallModules(AppModule::class)
@HiltAndroidTest
class CheckGoButtonClickLiveNavigationTest : BaseTest() {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
    )

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private val uiDevice = UiDevice.getInstance(getInstrumentation())

    @Test
    fun showGoButtonClickLiveNavigationTest() {
        try {
            enableGPS(ApplicationProvider.getApplicationContext())
            uiDevice.wait(Until.hasObject(By.desc(AMAZON_MAP_READY)), DELAY_15000)
            Thread.sleep(DELAY_2000)
            val cardDirection =
                mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_direction)
            if (cardDirection.visibility == View.VISIBLE) {
                val cardDirectionTest =
                    onView(withId(R.id.card_direction)).check(matches(isDisplayed()))
                cardDirectionTest.perform(click())
                uiDevice.wait(
                    Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/edt_search_direction")),
                    DELAY_5000,
                )
                val edtSearchDirection =
                    mActivityRule.activity.findViewById<TextInputEditText>(R.id.edt_search_direction)
                if (edtSearchDirection.visibility == View.VISIBLE) {
                    val clMyLocation =
                        onView(withId(R.id.cl_my_location)).check(matches(isDisplayed()))
                    clMyLocation.perform(click())

                    val rvSearchPlacesSuggestionDirection =
                        mActivityRule.activity.findViewById<RecyclerView>(R.id.rv_search_places_suggestion_direction)
                    onView(withId(R.id.edt_search_dest)).perform(ViewActions.typeText(TEST_WORD_4))
                    Thread.sleep(DELAY_2000)
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_search_places_suggestion_direction")),
                        DELAY_15000,
                    )
                    rvSearchPlacesSuggestionDirection.adapter?.itemCount?.let {
                        if (it > 0) {
                            onView(withId(R.id.rv_search_places_suggestion_direction)).perform(
                                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                                    0,
                                    click(),
                                ),
                            )
                        }
                    }
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_drive_go")),
                        DELAY_15000,
                    )
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_walk_go")),
                        DELAY_15000,
                    )
                    uiDevice.wait(
                        Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/card_truck_go")),
                        DELAY_20000,
                    )
                    val cardDriveGo =
                        mActivityRule.activity.findViewById<MaterialCardView>(R.id.card_drive_go)
                    if (cardDriveGo.visibility == View.VISIBLE) {
                        val cardDriveGoTest =
                            onView(withId(R.id.card_drive_go)).check(
                                matches(
                                    isDisplayed(),
                                ),
                            )
                        cardDriveGoTest.perform(click())

                        uiDevice.wait(
                            Until.hasObject(By.res("${BuildConfig.APPLICATION_ID}:id/rv_navigation_list")),
                            DELAY_15000,
                        )

                        val btnExit =
                            mActivityRule.activity.findViewById<AppCompatButton>(R.id.btn_exit)
                        Assert.assertTrue(TEST_FAILED_EXIT_BUTTON_NOT_VISIBLE, btnExit.visibility == View.VISIBLE)
                    } else {
                        Assert.fail(TEST_FAILED_CARD_DRIVE_GO)
                    }
                } else {
                    Assert.fail(TEST_FAILED_SEARCH_DIRECTION)
                }
            } else {
                Assert.fail(TEST_FAILED_DIRECTION_CARD)
            }
        } catch (e: Exception) {
            failTest(145, e)
            Assert.fail(TEST_FAILED)
        }
    }
}
