package com.systemnoxltd.hotelmatenox.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.systemnoxltd.hotelmatenox.AdUnits

class AppOpenAdManager(private val application: Application) : Application.ActivityLifecycleCallbacks {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false

    // toggle controlled by AdsViewModel (default true)
    private var shouldShowAds: Boolean = true

    init {
        application.registerActivityLifecycleCallbacks(this)
        loadAd()
    }

    fun setShouldShowAds(show: Boolean) {
        shouldShowAds = show
    }

    private fun loadAd() {
        if (appOpenAd != null || isLoadingAd) return
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            AdUnits.APP_OPEN,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    // ensure reload after dismissed
                    appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            appOpenAd = null
                            isShowingAd = false
                            loadAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            appOpenAd = null
                            isShowingAd = false
                            loadAd()
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    private fun showAdIfAvailable(activity: Activity) {
        if (!shouldShowAds) return
        if (isShowingAd) return
        val ad = appOpenAd ?: return
        isShowingAd = true
        ad.show(activity)
    }

    override fun onActivityResumed(activity: Activity) { showAdIfAvailable(activity) }

    // Other lifecycle callbacks (no-op)
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {}
    override fun onActivityPaused(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}
