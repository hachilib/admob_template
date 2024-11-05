package com.snowy.owl.lib.admob

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.snowy.owl.template.AdResume
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        lifecycleScope.launch {
            async { AdManager.loadAdsNative(this@SplashActivity,AdManager.nativeMain) }.await()
            async { AdManager.loadInter(this@SplashActivity,AdManager.inter) }.await()

            if (AdResume.getInstance().awaitAdLoad()) {
                AdResume.getInstance().showAdIfAvailable(
                    this@SplashActivity,
                    onAdsClose = {
                        nextActivity()
                    },
                    onAdsFailed = {
                        nextActivity()
                    },
                    onAdPaid = { adValue, adUnitAds->

                    }
                )
            } else {
                nextActivity(3000)
            }
        }
    }

    private fun nextActivity(delay: Long = 0){
        thread(start = true) {
            Thread.sleep(delay)
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}