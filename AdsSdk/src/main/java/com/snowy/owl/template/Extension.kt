package com.snowy.owl.template

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Window.hideSystemUI() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    WindowInsetsControllerCompat(this, this.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

fun showDialog(context: Context): Dialog {
    val dialog = Dialog(context)
    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_full_screen_onresume)
        setCancelable(false)
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
            hideSystemUI()
        }
    }
    try {
        if (!(context as Activity).isFinishing && !dialog.isShowing) {
            dialog.show()
        }
    } catch (e: Exception) {
        Log.d("LOG_AD_FAIL", "showDialog: Exception: ${e.message}")
    }
    return dialog
}

fun logFail(message: String) {
    Log.d("LOG_AD_FAIL", message)
}

fun handler(time: Long, onSuccess: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({ onSuccess() }, time)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}