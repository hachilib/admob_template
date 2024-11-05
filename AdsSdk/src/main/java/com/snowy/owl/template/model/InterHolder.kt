package com.snowy.owl.template.model

import com.google.android.gms.ads.interstitial.InterstitialAd

open class InterHolder(var ads: String) {
    var interAd : InterstitialAd?= null
    var isLoad = false
}