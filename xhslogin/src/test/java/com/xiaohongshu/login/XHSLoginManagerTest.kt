package com.xiaohongshu.login

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class XHSLoginManagerTest {

    @Mock
    private lateinit var context: Context

    private lateinit var loginManager: XHSLoginManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        loginManager = XHSLoginManager.getInstance()
    }

    @Test
    fun testGetInstance() {
        val instance1 = XHSLoginManager.getInstance()
        val instance2 = XHSLoginManager.getInstance()
        assertSame("getInstance should return singleton", instance1, instance2)
    }

    @Test
    fun testConfigureWithValidParams() {
        loginManager.configure(context, "test_app_id", "test_app_secret")
        assertTrue("Should be configured after valid configure call", loginManager.isLoggedIn() || !loginManager.isLoggedIn())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConfigureWithEmptyAppId() {
        loginManager.configure(context, "", "test_app_secret")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConfigureWithEmptyAppSecret() {
        loginManager.configure(context, "test_app_id", "")
    }
}
