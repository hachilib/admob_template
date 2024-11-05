package com.snowy.owl.lib.admob

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.snowy.owl.lib.admob.databinding.ActivityMainBinding
import com.snowy.owl.template.AdmobUtils.isNetworkConnected

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.tv.setOnClickListener {
            AdManager.showInter(this,AdManager.inter){
                startActivity(Intent(this, MainActivity2::class.java))
            }
        }

        if (isNetworkConnected()) {
            AdManager.showNative(binding.nativeAd, AdManager.nativeMain)
            AdManager.loadBanner(binding.bannerAd,"")
        }
    }
}