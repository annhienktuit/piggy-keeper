package com.annhienktuit.piggykeeper

import android.provider.Telephony
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.annhienktuit.piggykeeper.activity.LoginActivity
import com.annhienktuit.piggykeeper.activity.MainActivity
import com.annhienktuit.piggykeeper.activity.SignUpActivity
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class LoginTest {
    @Rule @JvmField
    public val mActivityRule: ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java)
    val mainActivityRule = IntentsTestRule(SignUpActivity::class.java)
    @Before
    fun setUp(){
        Intents.init()
        mActivityRule.activity

    }
    @Test
    fun testInputEmailField(){
        onView(withId(R.id.edtEmail)).perform(typeText("demose114@gmail.com"),closeSoftKeyboard())
        onView(withId(R.id.edtPassword)).perform(typeText("Nhien2001"),closeSoftKeyboard())
        onView(withId(R.id.textViewDoSignUp)).perform(click())
        intended(hasComponent(SignUpActivity::class.java!!.name))
    }
    @After
    fun release(){
        Intents.release()
    }
}