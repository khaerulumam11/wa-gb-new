package com.whatsapp.chattema;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;

public class UnduhActivity extends AppIntro2 {

    private InterstitialAd mInterstitialAd;
    private View bannerView;
    int fragmentSeq = 0;
    int seq = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadInter();

        AdView adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId(getString(R.string.admob_banner));
        getAddholder().addView(adView);
        adView.loadAd(new Utils().getAdsRequest(this));

        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_1)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_2)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_3)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_4)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_5)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_6)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_7)
        );
        addSlide(
                CommonSlideFragment.newInstance("Unduh Tema-WA", "Preview Tampilan tema yang dapat Anda pakai ketika pemasangan berhasil", R.drawable.unduh_8)
        );

        addSlide(
                UnduhFragment.newInstance()
        );

        UnityBanners.setBannerListener(new IUnityBannerListener() {
            @Override
            public void onUnityBannerLoaded(String s, View view) {
                bannerView = view;
                if (getAddholder().getChildCount() == 0)
                    getAddholder().addView(view);
                else {
                    getAddholder().removeViewAt(0);
                    getAddholder().addView(view);
                }
            }

            @Override
            public void onUnityBannerUnloaded(String s) {
                bannerView = null;
            }

            @Override
            public void onUnityBannerShow(String s) {

            }

            @Override
            public void onUnityBannerClick(String s) {

            }

            @Override
            public void onUnityBannerHide(String s) {

            }

            @Override
            public void onUnityBannerError(String s) {

            }
        });
        UnityBanners.loadBanner (this, getString(R.string.unity_banner));


        showSkipButton(false);


    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        fragmentSeq += 1;
        if(fragmentSeq % seq == 0){
            if (mInterstitialAd!= null) {
                mInterstitialAd.show(this);
                loadInter();
            }else {
                if (UnityAds.isReady(getString(R.string.unity_inter))) {
                    UnityAds.show(this, getString(R.string.unity_inter));
                }
            }
        }

    }


    void loadInter() {
        InterstitialAd.load(this,getString(R.string.admob_intersitial), new Utils().getAdsRequest(this),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });
    }

}
