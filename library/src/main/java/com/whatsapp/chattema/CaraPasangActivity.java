package com.whatsapp.chattema;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;
import com.google.ads.mediation.unity.UnityInitializer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;

public class CaraPasangActivity extends AppIntro2 {

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
        UnityAds.initialize(this, getString(R.string.unity_app_id), new IUnityAdsListener() {
            @Override
            public void onUnityAdsReady(String s) {
                Log.i("Unity", "ads ready "+s);
            }

            @Override
            public void onUnityAdsStart(String s) {
                Log.i("Unity", "ads start "+s);
            }

            @Override
            public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
                Log.i("Unity", "ads finish "+s);
            }

            @Override
            public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
                Log.i("Unity", "ads error "+s);
            }
        }, false);
        getAddholder().addView(adView);
        adView.loadAd(new Utils().getAdsRequest(this));

        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Unduh aplikasi terlebih dahulu, jika belum lihat Cara Unduh pada menu di aplikasi ini. Pastikan proses pengunduhan selesa",
                        R.drawable.i1)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Pilih pasang atau instal. Tunggu sampai proses pemasangan selesai. Proses pemasangan membutuhkan waktu 1-3 menit",
                        R.drawable.i2)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Jika sudah selesai, pilih buka",
                        R.drawable.i3)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Pilih setuju dan lanjutkan",
                        R.drawable.i4)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Pilih pulihkan cadangan chat atau tidak. Jika Anda pilih Restore, maka aplikasi akan memuat kembali data percakapan Anda. Namun jika pilih skip maka riwayat percakapan tidak akan dimuat kembali. Pilih sesuai kebutuhan",
                        R.drawable.i5)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Masukkan nomor telepon Anda",
                        R.drawable.i6)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Pasang",
                        "Jika nomor sudah benar, lakukan verfikasi. Kode verifikasi akan dikirim ke nomor hp Anda, maka pastikan nomor tersebut aktif. Jika belum jelas, silahkan tonton video tutorialnya pada menu Video Panduan",
                        R.drawable.i7, true)
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
            if (mInterstitialAd!=null) {
                mInterstitialAd.show(this);
                loadInter();
            } else {
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
        if (UnityAds.isReady(getString(R.string.unity_inter))) {
            UnityAds.show(this, getString(R.string.unity_inter));
        }
    }

}
