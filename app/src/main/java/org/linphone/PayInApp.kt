package org.linphone

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.anupkumarpanwar.scratchview.ScratchView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.ads.rewarded.RewardItem
import kotlinx.android.synthetic.main.activity_pay_in_app.*

class PayInApp : AppCompatActivity(), RewardedVideoAdListener {

    lateinit var mRewardedVideoAd: RewardedVideoAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_in_app)

        MobileAds.initialize(this)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)

        mRewardedVideoAd.loadAd(
            "ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build()
        )

        val cardView = findViewById<CardView>(R.id.cardView)

        val scratchView = findViewById<ScratchView>(R.id.scratch_view)
        scratchView.setRevealListener(object : ScratchView.IRevealListener {
            override fun onRevealed(scratchView: ScratchView) {
                Toast.makeText(applicationContext, "revelead", Toast.LENGTH_LONG).show()

                val timer = object : CountDownTimer(4000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        Toast.makeText(applicationContext, "revelead AGAIN", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@PayInApp, Reward::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                timer.start()
            }
            override fun onRevealPercentChangedListener(scratchView: ScratchView, percent: Float) {
                Log.d("Revealed", percent.toString())
            }
        })

        // using Banner ads
        val mAdView = findViewById<AdView> (R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // using Interstitial ads
        val mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        clickButton.setOnClickListener {
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                Log.d("TAG", "lost")
            }
        }

        // using Rewarded ads

        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()
        clickButtonR.setOnClickListener {
            if (mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }
        }
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
            "ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build()
        )
    }

    override fun onRewardedVideoAdClosed() {
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show()
    }

    override fun onRewarded(p0: com.google.android.gms.ads.reward.RewardItem?) {
        TODO("Not yet implemented")
    }

    override fun onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdLoaded() {
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdOpened() {
        TODO("Not yet implemented")
    }

    override fun onRewardedVideoCompleted() {
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show()
        cardView.visibility = View.VISIBLE
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Toast.makeText(this, "FAILED TO LOAD", Toast.LENGTH_SHORT).show()
    }
}
