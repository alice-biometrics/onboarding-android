package com.alicebiometrics.aliceonboardingsampleapp


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
@LargeTest
class ExampleInstrumentedTest {

    @Rule @JvmField
    val mActivityRule = ActivityTestRule(MainActivity::class.java)

    @get:Rule var permissionRuleCamera = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)
    @get:Rule var permissionRuleWriteExternal = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


    var mLocatingActivity: MainActivity? = null

    @Before
    fun setup() {
        mLocatingActivity = mActivityRule.getActivity()
    }

    @Test fun listGoesOverTheFold() {
        onView(withId(R.id.buttonCreateAccount)).check(matches(isDisplayed()))
    }
}