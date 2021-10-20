package com.whatsapp.chattema;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;

public class CaraUnduhActivity extends AppIntro2 {

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
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Pilih menu 'Themes' pada menu bawah paling kanan saat aplikasi pertama dibuka",
                        R.drawable.d1)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Pilih menu atau tombol paling atas dengan tulisan 'UNDUH'",
                        R.drawable.d2)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Pilih tombol panah ke kanan atau next untuk terus melihat preview atau tampilan-tampilan tema ketika Anda berhasil memasang aplikasi",
                        R.drawable.d3)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Pada halaman paling akhir, pilih menu paling atas yaitu tombol dengan tulisan 'UN D U H'",
                        R.drawable.d4)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Anda akan diminta untuk menonton video promosi dari mitra, tonton video sampai selesai untuk sampai ke halaman Unduh. Klik 'Ok' untuk melanjutkan",
                        R.drawable.d5)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Jika sudah menonton video sampai selesai, tutup halaman video maka akan otomatis masuk ke halaman Unduh. Harap dibaca dengan seksama petunjuk yang ada di halaman tersebut agar Anda memahami proses pemasangan dan kelebihan-kelebihan GBWA. Scroll sampai bawah untuk mengunduh",
                        R.drawable.d6)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Pilih seperti pada gambar, sesuaikan dengan kebutuhan Anda mana yang akan diunduh (harap dipahami petunjuk dari halaman tersebut)",
                        R.drawable.d7)
        );
        addSlide(
                CommonSlideFragment.newInstance("Cara Unduh",
                        "Proses unduh akan berjalan, harap tunggu sampai selesai. Jika selesai maka lanjutkan kedalam proses pemasangan. Jika belum jelas, silahkan tonton video tutorialnya pada menu Video Panduan",
                        R.drawable.d8, true)
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
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                loadInter();
            }else {
                if (UnityAds.isReady(getString(R.string.unity_inter))) {
                    UnityAds.show(this, getString(R.string.unity_inter));
                }
            }
        }

    }


    void loadInter() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_intersitial));
        mInterstitialAd.loadAd(new Utils().getAdsRequest(this));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdClosed() {
                // mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
        if (UnityAds.isReady(getString(R.string.unity_inter))) {
            UnityAds.show(this, getString(R.string.unity_inter));
        }
    }

}
