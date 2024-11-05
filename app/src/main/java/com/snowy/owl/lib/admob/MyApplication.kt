package com.snowy.owl.lib.admob

import android.app.Application
import com.snowy.owl.template.AdResume
import com.snowy.owl.template.AdmobUtils

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        AdmobUtils.initAdmob(this, isDebug = true, isEnabledAds = true)
        AdResume.getInstance().init(this)
        AdResume.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
    }
}