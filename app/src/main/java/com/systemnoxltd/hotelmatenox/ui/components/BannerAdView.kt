package com.systemnoxltd.hotelmatenox.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.systemnoxltd.hotelmatenox.AdUnits

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdUnits.BANNER
) {
    val context = LocalContext.current
    val adSize = remember {
        val density = context.resources.displayMetrics.density
        val adWidthPixels = context.resources.displayMetrics.widthPixels.toFloat()
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context,
            (adWidthPixels / density).toInt()
        )
    }
    AndroidView(
        modifier = modifier,
        factory = {
            AdView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setAdSize(adSize)
//                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
