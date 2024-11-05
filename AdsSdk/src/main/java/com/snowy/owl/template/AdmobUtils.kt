package com.snowy.owl.template

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.ViewGroup
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.snowy.owl.template.databinding.LayoutBannerLoadingBinding
import com.snowy.owl.template.databinding.LayoutNativeLoadingMediumBinding
import com.snowy.owl.template.databinding.LayoutNativeLoadingSmallBinding
import com.snowy.owl.template.model.InterHolder
import com.snowy.owl.template.model.NativeHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

object AdmobUtils {

    var isDebug = true
    var isEnabledAds = true

    var adRequest: AdRequest? = null
    var adRequest2: AdRequest? = null

    var shimmerFrameLayout: ShimmerFrameLayout? = null

    private var dialogLoading: Dialog? = null


    fun initAdmob(
        context: Context,
        isDebug: Boolean = true,
        isEnabledAds: Boolean = true,
        timeOut: Int = 10000
    ) {
        this.isDebug = isDebug
        this.isEnabledAds = isEnabledAds
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(context) {}
            initListIdTest()
            adRequest = AdRequest.Builder()
                .setHttpTimeoutMillis(timeOut)
                .build()
        }
    }

    private fun initListIdTest() {
        /*testDevices.add("727D4F658B63BDFA0EFB164261AAE54")*/
    }

    @JvmStatic
    fun Context.isNetworkConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @JvmStatic
    fun loadNativeAd(
        context: Context,
        holder: NativeHolder,
        onAdFail: ((value: String) -> Unit)? = null,
        onAdPaid: ((adValue: AdValue) -> Unit)? = null,
        onLoadAndGetNative: ((nativeAd: NativeAd) -> Unit)? = null
    ) {
        if (!isEnabledAds) {
            logFail("Load native: Enabled Ads")
            onAdFail?.invoke("Load native: Enabled Ads")
            return
        }

        if (!context.isNetworkConnected()) {
            logFail("Load native: No Internet")
            onAdFail?.invoke("Load native: No Internet")
            return
        }

        if (isDebug) holder.ads = context.getString(R.string.native_ad)
        holder.isLoad = false

        val adLoader = AdLoader.Builder(context, holder.ads)
            .forNativeAd { ad: NativeAd ->
                holder.isLoad = true
                holder.nativeAd = ad

                ad.setOnPaidEventListener { it: AdValue -> onAdPaid?.invoke(it) }
                onLoadAndGetNative?.invoke(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    holder.isLoad = false
                    holder.nativeAd = null
                    onAdFail?.invoke("Load native: NativeFailedToLoad: ${adError.message}")
                    logFail("Load native: ${adError.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adRequest?.let { adLoader.loadAd(it) }
    }

    @JvmStatic
    fun showNativeAd(
        holder: NativeHolder,
        view: ViewGroup,
        layout: Int,
        onNativeFail: ((value: String) -> Unit)? = null,
        onNativeLoaded: (() -> Unit)? = null,
        onNativePaid: ((adValue: AdValue, ad: String) -> Unit)? = null
    ) {
        if (!isEnabledAds) {
            logFail("Show native: Enabled Ads")
            onNativeFail?.invoke("Show native: Enabled Ads")
            return
        }

        if (!holder.isLoad) {
            onNativeFail?.invoke("Show native: Load Fail")
            logFail("Show native: Load Fail")
            return
        }

        if (holder.nativeAd == null) {
            onNativeFail?.invoke("Show native: NativeAd null")
            logFail("Show native: NativeAd null")
            return
        }

        view.removeAllViews()
        holder.nativeAd?.let { nativeAd ->
            nativeAd.setOnPaidEventListener { it: AdValue -> onNativePaid?.invoke(it, holder.ads) }
            val adView =
                (view.context as Activity).layoutInflater.inflate(layout, null) as NativeAdView
            NativeView.populateNativeAdView(nativeAd, adView)

            view.removeAllViews()
            view.addView(adView)
            onNativeLoaded?.invoke()
        }
    }

    fun loadAndShowNative(
        holder: NativeHolder,
        view: ViewGroup,
        layout: Int,
        isMedium: Boolean = true,
        onNativeFail: ((value: String) -> Unit)? = null,
        onNativePaid: ((adValue: AdValue, ad: String) -> Unit)? = null,
        onNativeShow: (() -> Unit)? = null,
    ) {
        val activity = view.context as Activity

        if (!isEnabledAds) {
            logFail("Load and show native: Enabled Ads")
            onNativeFail?.invoke("Load and show native: Enabled Ads")
            return
        }

        if (!activity.isNetworkConnected()) {
            logFail("Load and show native: No Internet")
            onNativeFail?.invoke("Load and show native: No Internet")
            return
        }

        if (isDebug) holder.ads = activity.getString(R.string.native_ad_video)

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout?.stopShimmer()
        }

        view.removeAllViews()
        val tagView = if (isMedium) {
            LayoutNativeLoadingMediumBinding.inflate(activity.layoutInflater)
        } else {
            LayoutNativeLoadingSmallBinding.inflate(activity.layoutInflater)
        }
        view.addView(tagView.root, 0)
        if (shimmerFrameLayout == null) shimmerFrameLayout = tagView.root as ShimmerFrameLayout
        shimmerFrameLayout?.startShimmer()

        val adLoader = AdLoader.Builder(activity, holder.ads)
            .forNativeAd { nativeAd: NativeAd ->

                nativeAd.setOnPaidEventListener { onNativePaid?.invoke(it, holder.ads) }
                val adView =
                    (view.context as Activity).layoutInflater.inflate(layout, null) as NativeAdView
                NativeView.populateNativeAdView(nativeAd, adView)

                shimmerFrameLayout?.stopShimmer()
                view.removeAllViews()
                view.addView(adView)
                onNativeShow?.invoke()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onNativeFail?.invoke("Load and show native: NativeFailedToLoad: ${adError.message}")
                    logFail("Load and show native: NativeFailedToLoad: ${adError.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adRequest?.let { adLoader.loadAd(it) }
    }

    fun loadAndShowBanner(
        view: ViewGroup,
        idBanner: String,
        isCollapsible: Boolean = false,
        isBottom: Boolean = true,
        onBannerFail: ((value: String) -> Unit)? = null,
        onBannerPaid: ((adValue: AdValue, ad: String) -> Unit)? = null,
        onBannerShow: (() -> Unit)? = null,
    ) {
        val activity = view.context as Activity

        if (!isEnabledAds) {
            logFail("Load and show banner: Enabled Ads")
            onBannerFail?.invoke("Load and show banner: Enabled Ads")
            return
        }

        if (!activity.isNetworkConnected()) {
            logFail("Load and show banner: No Internet")
            onBannerFail?.invoke("Load and show banner: No Internet")
            return
        }

        val id = if (isDebug) activity.getString(R.string.banner_ad) else idBanner

        if (shimmerFrameLayout != null) {
            shimmerFrameLayout?.stopShimmer()
        }

        view.removeAllViews()
        val tagView = LayoutBannerLoadingBinding.inflate(activity.layoutInflater)

        view.addView(tagView.root, 0)
        if (shimmerFrameLayout == null) shimmerFrameLayout = tagView.root
        shimmerFrameLayout?.startShimmer()

        val adView = AdView(activity).apply {
            adUnitId = id
            setAdSize(getAdSize(activity))
        }

        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                shimmerFrameLayout?.stopShimmer()
                view.removeAllViews()
                onBannerFail?.invoke("Load and show banner: AdFailedToLoad: ${adError.message}")
            }

            override fun onAdLoaded() {
                shimmerFrameLayout?.stopShimmer()
                view.removeAllViews()
                view.addView(adView)
                adView.setOnPaidEventListener { onBannerPaid?.invoke(it, idBanner) }
                onBannerShow?.invoke()
            }
        }

        if (isCollapsible) {
            if (adRequest2 == null) {
                val extras = Bundle().apply {
                    putString("collapsible", if (isBottom) "bottom" else "top")
                }
                adRequest2 = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()
            }
            adView.loadAd(adRequest2!!)
        } else {
            adRequest?.let { adView.loadAd(it) }
        }

    }

    private fun getAdSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun loadInterAd(
        context: Context,
        inter: InterHolder,
        onInterFail: ((String) -> Unit)? = null,
        onInterLoaded: (() -> Unit)? = null
    ) {
        if (!isEnabledAds) {
            logFail("Load inter: Enabled Ads")
            onInterFail?.invoke("Load inter: Enabled Ads")
            return
        }

        if (!context.isNetworkConnected()) {
            logFail("Load inter: No Internet")
            onInterFail?.invoke("Load inter: No Internet")
            return
        }

        val id = if (isDebug) context.getString(R.string.inter_ad) else inter.ads

        inter.isLoad = false

        adRequest?.let { request ->
            InterstitialAd.load(context, id, request, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    inter.interAd = null
                    inter.isLoad = false
                    logFail("Load inter: AdFailedToLoad: ${adError.message}")
                    onInterFail?.invoke("Load inter: AdFailedToLoad: ${adError.message}")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    inter.interAd = interstitialAd
                    inter.isLoad = true
                    onInterLoaded?.invoke()
                }
            })
        }
    }

    fun showInterAd(
        activity: Activity,
        interHolder: InterHolder,
        onAdClose: (() -> Unit)? = null,
        onAdShow: (() -> Unit)? = null,
        onInterFail: ((String) -> Unit)? = null,
        onInterPaid: ((adValue: AdValue, ad: String) -> Unit)? = null,
    ) {
        if (!isEnabledAds) {
            logFail("Show inter: Enabled Ads")
            onInterFail?.invoke("Show inter: Enabled Ads")
            return
        }

        AdResume.getInstance().disableAppResumeWithActivity(activity::class.java)

        interHolder.interAd?.let { interAd ->
            interAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interHolder.interAd = null
                    onAdClose?.invoke()
                    AdResume.getInstance().enableAppResumeWithActivity(activity::class.java)
                    loadInterAd(activity, interHolder)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interHolder.interAd = null
                    logFail("Load inter: onAdFailedToShowFullScreenContent: ${adError.message}")
                    onInterFail?.invoke("Load inter: onAdFailedToShowFullScreenContent: ${adError.message}")
                    AdResume.getInstance().enableAppResumeWithActivity(activity::class.java)
                    loadInterAd(activity, interHolder)
                }

                override fun onAdShowedFullScreenContent() {
                    interAd.setOnPaidEventListener { onInterPaid?.invoke(it, interHolder.ads) }
                    onAdShow?.invoke()
                }

            }

            dialogLoading = showDialog(activity)
            handler(800) {
                interAd.show(activity)
                handler(200) {
                    dialogLoading?.dismiss()
                    dialogLoading = null
                }
            }
        }
    }

}