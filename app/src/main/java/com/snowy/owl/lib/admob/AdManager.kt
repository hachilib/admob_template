package com.snowy.owl.lib.admob

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.AdValue
import com.snowy.owl.template.AdmobUtils
import com.snowy.owl.template.model.InterHolder
import com.snowy.owl.template.model.NativeHolder

object AdManager {

    val nativeMain = NativeHolder("ca-app-pub-3940256099942544/2247696110")
    val inter = InterHolder("ca-app-pub-3940256099942544/2247696110")

    @JvmStatic
    fun loadAdsNative(context: Context, holder: NativeHolder) {
        AdmobUtils.loadNativeAd(context, holder)
    }

    @JvmStatic
    fun showNative(viewGroup: ViewGroup, holder: NativeHolder) {
        AdmobUtils.showNativeAd(
            holder,
            viewGroup,
            R.layout.ad_template_medium,
            onNativeLoaded = {

            },
            onNativeFail = {

            },
            onNativePaid = { adValue: AdValue, ad: String ->

            }
        )
    }

    @JvmStatic
    fun loadShowNative(viewGroup: ViewGroup, holder: NativeHolder) {
        AdmobUtils.loadAndShowNative(
            holder,
            viewGroup,
            R.layout.ad_template_medium,
            onNativeShow = {

            },
            onNativeFail = {

            },
            onNativePaid = { adValue: AdValue, ad: String ->

            }
        )
    }

    @JvmStatic
    fun loadBanner(viewGroup: ViewGroup, idBanner: String) {
        AdmobUtils.loadAndShowBanner(
            viewGroup,
            idBanner,
            onBannerFail = {

        })
    }

    @JvmStatic
    fun loadBannerCollapsible(viewGroup: ViewGroup, idBanner: String) {
        AdmobUtils.loadAndShowBanner(
            viewGroup,
            idBanner,
            isCollapsible = true,
            isBottom = true,
            onBannerFail = {

            })
    }

    @JvmStatic
    fun loadInter(context: Context, interHolder: InterHolder){
        AdmobUtils.loadInterAd(context,interHolder)
    }

    @JvmStatic
    fun showInter(activity: Activity, interHolder: InterHolder, onAdClose: ()->Unit){
        AdmobUtils.showInterAd(activity,interHolder,onAdClose)
    }
}