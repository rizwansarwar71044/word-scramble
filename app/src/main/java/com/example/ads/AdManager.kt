package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError

class AdManager(private val context: Context) {

    private val adContext: Context = context.applicationContext

    private var rewardedAd: RewardedAd? = null
    var isLoadingAd = false
        private set

    companion object {
        private const val TAG = "AdManager"
        // Rewarded Ad Unit ID
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-2241360064247668/7459586561"
    }

    init {
        try {
            // Initialize Mobile Ads SDK
            MobileAds.initialize(adContext) { status ->
                Log.d(TAG, "AdMob Initialized: $status")
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob: ${e.message}", e)
        }
    }

    fun loadRewardedAd() {
        if (rewardedAd != null || isLoadingAd) return

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            adContext,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isLoadingAd = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad loaded successfully.")
                    rewardedAd = ad
                    isLoadingAd = false
                }
            }
        )
    }

    fun isAdReady(): Boolean {
        return rewardedAd != null
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed full screen content.")
                    rewardedAd = null
                    // Pre-load the next ad
                    loadRewardedAd()
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    onAdFailed()
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            Log.d(TAG, "Ad was not ready yet. Firing failed callback to trigger simulated ad fallback.")
            onAdFailed()
        }
    }
}
