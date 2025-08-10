package com.systemnoxltd.hotelmatenox.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.systemnoxltd.hotelmatenox.AdUnits

class InterstitialAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null

    fun load(adUnitId: String) {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, request, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                interstitialAd = null
            }
        })
    }

    /**
     * Shows the ad if loaded; otherwise immediately calls onComplete().
     * onComplete runs on ad dismiss (or immediately if no ad).
     */
    fun show(activity: Activity, onComplete: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            onComplete()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onComplete()
                // reload for next time
                load(AdUnits.INTERSTITIAL)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                onComplete()
            }
        }
        ad.show(activity)
    }
}
