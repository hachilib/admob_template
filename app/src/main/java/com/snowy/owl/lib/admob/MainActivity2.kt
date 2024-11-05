package com.snowy.owl.lib.admob

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.snowy.owl.lib.admob.databinding.ActivityMain2Binding
import com.snowy.owl.lib.admob.databinding.ActivityMainBinding
import com.snowy.owl.template.AdmobUtils.isNetworkConnected

class MainActivity2 : AppCompatActivity() {

    private val binding by lazy { ActivityMain2Binding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (isNetworkConnected()) {
            AdManager.loadShowNative(binding.nativeLoadAndShowAd, AdManager.nativeMain)
            AdManager.loadBannerCollapsible(binding.bannerAd, "")
        }
    }
}