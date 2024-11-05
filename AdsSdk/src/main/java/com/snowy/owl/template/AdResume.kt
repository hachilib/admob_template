package com.snowy.owl.template

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CompletableDeferred
import java.util.Date

class AdResume : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private val LOG_TAG = "TAGGG"

    lateinit var adRequest: AdRequest
    lateinit var myApplication: Application


    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    private var appResumeAdId: String? = null

    private var loadTime: Long = 0
    private var currentActivity: Activity? = null

    private var dialogLoading: Dialog? = null

    private var disabledAppOpenList: ArrayList<Class<*>>? = ArrayList()

    private var adLoadDeferred: CompletableDeferred<Boolean>? = null

    fun disableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(LOG_TAG, "disableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList!!.add(activityClass)
    }

    fun enableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(LOG_TAG, "enableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList!!.remove(activityClass)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var INSTANCE: AdResume

        @Synchronized
        fun getInstance(): AdResume {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = AdResume()
            }
            return INSTANCE
        }
    }

    fun init(
        application: Application,
        appOpenAdId: String = application.getString(R.string.test_ad_admob_app_open)
    ) {
        if (!AdmobUtils.isEnabledAds) {
            Log.d(LOG_TAG, "init: DisabledAds")
            return
        }

        adLoadDeferred = CompletableDeferred()

        this.myApplication = application
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(5000)
            .build()

        this.appResumeAdId = appOpenAdId

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        this.myApplication.registerActivityLifecycleCallbacks(this)
        if (!isAdAvailable()) loadAd()
    }

    suspend fun awaitAdLoad(): Boolean {
        if (isAdAvailable()) return true
        return adLoadDeferred?.await() ?: false
    }

    private fun loadAd() {
        if (isLoadingAd || isAdAvailable()) return

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            myApplication,
            appResumeAdId ?: myApplication.getString(R.string.test_ad_admob_app_open),
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(LOG_TAG, "Ad was loaded.")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time

                    adLoadDeferred?.complete(true)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(LOG_TAG, loadAdError.message)
                    isLoadingAd = false

                    adLoadDeferred?.complete(false)
                }
            })
    }

    fun showAdIfAvailable(
        activity: Activity,
        onAdsClose: (() -> Unit)? = null,
        onAdsFailed: (() -> Unit)? = null,
        onAdPaid: ((adValue: AdValue, adUnitAds: String) -> Unit)? = null,
    ) {
        if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            Log.d(LOG_TAG, "STARTED")
            return
        }
        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }

        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            loadAd()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG, "Ad dismissed fullscreen content.")
                appOpenAd = null
                isShowingAd = false

                onAdsClose?.invoke()
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(LOG_TAG, adError.message)
                appOpenAd = null
                isShowingAd = false

                onAdsFailed?.invoke()
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(LOG_TAG, "Ad showed fullscreen content.")
            }
        }

        dialogLoading = showDialog(activity)
        handler(800) {
            isShowingAd = true
            appOpenAd?.run {
                setOnPaidEventListener { onAdPaid?.invoke(it, adUnitId) }
                show(activity)
            }
            handler(200) {
                dialogLoading?.dismiss()
                dialogLoading = null
            }
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }


    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        currentActivity?.let { p ->

            if (!AdmobUtils.isEnabledAds) {
                return
            }

            disabledAppOpenList?.let { activityList ->
                for (activity in activityList) {
                    if (activity.name == p.javaClass.name) {
                        Log.d(LOG_TAG, "onStart: activity is disabled")
                        return
                    }
                }
            }

            showAdIfAvailable(p)
        }
    }


    override fun onActivityCreated(activity: Activity, p1: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
        dialogLoading?.let { d -> if (d.isShowing) d.dismiss() }
    }



}