package com.snowy.owl.template

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.snowy.owl.template.databinding.AdUnifiedBinding

object NativeView {
    fun populateNativeAdView(nativeAd: NativeAd, unifiedAdBinding: AdUnifiedBinding) {
        val nativeAdView = unifiedAdBinding.root

        nativeAdView.mediaView = unifiedAdBinding.adMedia

        nativeAdView.headlineView = unifiedAdBinding.adHeadline
        nativeAdView.bodyView = unifiedAdBinding.adBody
        nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
        nativeAdView.iconView = unifiedAdBinding.adAppIcon
        nativeAdView.priceView = unifiedAdBinding.adPrice
        nativeAdView.starRatingView = unifiedAdBinding.adStars
        nativeAdView.storeView = unifiedAdBinding.adStore
        nativeAdView.advertiserView = unifiedAdBinding.adAdvertiser

        unifiedAdBinding.adHeadline.text = nativeAd.headline
        nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.mediaContent = it }

        if (nativeAd.body == null) {
            unifiedAdBinding.adBody.invisible()
        } else {
            unifiedAdBinding.adBody.visible()
            unifiedAdBinding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            unifiedAdBinding.adCallToAction.invisible()
        } else {
            unifiedAdBinding.adCallToAction.visible()
            unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            unifiedAdBinding.adAppIcon.visibility = View.GONE
        } else {
            unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            unifiedAdBinding.adAppIcon.visible()
        }

        if (nativeAd.price == null) {
            unifiedAdBinding.adPrice.invisible()
        } else {
            unifiedAdBinding.adPrice.visible()
            unifiedAdBinding.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            unifiedAdBinding.adStore.invisible()
        } else {
            unifiedAdBinding.adStore.visible()
            unifiedAdBinding.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            unifiedAdBinding.adStars.invisible()
        } else {
            unifiedAdBinding.adStars.rating = nativeAd.starRating!!.toFloat()
            unifiedAdBinding.adStars.visible()
        }

        if (nativeAd.advertiser == null) {
            unifiedAdBinding.adAdvertiser.invisible()
        } else {
            unifiedAdBinding.adAdvertiser.text = nativeAd.advertiser
            unifiedAdBinding.adAdvertiser.visible()
        }

        nativeAdView.setNativeAd(nativeAd)

        val mediaContent = nativeAd.mediaContent
        val vc = mediaContent?.videoController

        if (vc != null && mediaContent.hasVideoContent()) {
            vc.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {
                    override fun onVideoEnd() {
                        //mainActivityBinding.refreshButton.isEnabled = true
                        //mainActivityBinding.videostatusText.text = "Video status: Video playback has ended."
                        super.onVideoEnd()
                    }
                }
        } else {
            //mainActivityBinding.videostatusText.text = "Video status: Ad does not contain a video asset."
            //mainActivityBinding.refreshButton.isEnabled = true
        }
    }

    fun populateNativeAdView(
        nativeAd: NativeAd,
        adView: NativeAdView,
    ) {
        adView.findViewById<MediaView>(R.id.ad_media)?.let {
            adView.mediaView = it
        }
        adView.findViewById<TextView>(R.id.ad_headline)?.let {
            adView.headlineView = it
        }
        adView.findViewById<TextView>(R.id.ad_body)?.let {
            adView.bodyView = it
        }
        adView.findViewById<Button>(R.id.ad_call_to_action)?.let {
            adView.callToActionView = it
        }
        adView.findViewById<ImageView>(R.id.ad_app_icon)?.let {
            adView.iconView = it
        }
        adView.findViewById<RatingBar>(R.id.ad_stars)?.let {
            adView.starRatingView = it
        }
        if (nativeAd.mediaContent != null) {
            adView.mediaView?.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
            adView.mediaView?.mediaContent = nativeAd.mediaContent
        }

        if (nativeAd.headline != null) {
            (adView.headlineView as TextView).text = nativeAd.headline
        }
        if (nativeAd.body == null) {
            adView.bodyView?.invisible()
        } else {
            adView.bodyView?.visible()
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.invisible()

        } else {
            adView.callToActionView?.visible()
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }


        if (adView.iconView != null) {
            if (nativeAd.icon == null) {
                adView.iconView?.gone()
            } else {
                (adView.iconView as ImageView).setImageDrawable(
                    nativeAd.icon?.drawable
                )
                adView.iconView?.visible()
            }
        }

        if (nativeAd.starRating != null) {
            (adView.starRatingView as RatingBar).rating = 5f
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent?.videoController
        if (vc?.hasVideoContent() != null) {
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
            }
        }
    }

}