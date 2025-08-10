package com.systemnoxltd.hotelmatenox

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.systemnoxltd.hotelmatenox.ads.AppOpenAdManager

class MyApplication : Application() {
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        // Optional: configure test device(s) while debugging
        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("TEST_DEVICE_ID")) // replace with your test device id if needed
            .build()
        MobileAds.setRequestConfiguration(config)

        appOpenAdManager = AppOpenAdManager(this) // registers lifecycle callbacks
    }
}
