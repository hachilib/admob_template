package com.snowy.owl.template.model

import com.google.android.gms.ads.nativead.NativeAd

open class NativeHolder(var ads: String) {
    var nativeAd : NativeAd?= null
    var isLoad = false
}