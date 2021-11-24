package com.whatsapp.chattema;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.unity3d.ads.UnityAds;

public class UnduhFragment extends Fragment {


    private Button mUnduhButton;
    private RewardedAd rewardedAd;

    private boolean isEarned = false;
    private int tryToOpen = 0;
    private final String GBURL = "https://s.id/ptgbwa";


    public UnduhFragment() {

    }


    public static UnduhFragment newInstance() {
        UnduhFragment fragment = new UnduhFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_unduh, container, false);
        mUnduhButton = rootView.findViewById(R.id.unduh);

        loadReward();

        AdLoader adLoader = new AdLoader.Builder(getContext(), getContext().getString(R.string.admob_native))
                .forNativeAd(unifiedNativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();

                    TemplateView template = rootView.findViewById(R.id.my_template);
                    template.setStyles(styles);
                    template.setNativeAd(unifiedNativeAd);

                })
                .build();

        adLoader.loadAd(new Utils().getAdsRequest(getActivity()));

        mUnduhButton.setOnClickListener(view -> new MaterialAlertDialogBuilder(getContext(), R.style.Theme_MaterialComponents_Dialog)
                .setTitle("Unduh Tema-WA")
                .setMessage("Untk melanjutkan proses Un d u h  tema, silahkan tonton video promosi dari mitra kami sampai selesai")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    showReward();
                })
                .show());

        return rootView;
    }

    private void showReward(){
        if (rewardedAd!=null) {
            OnUserEarnedRewardListener onUserEarnedRewardListener = new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    isEarned = true;
                }
            };
            RewardedAdLoadCallback rewardedAdLoadCallback = new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                    super.onAdLoaded(rewardedAd);
                    if(!isEarned) {
                        Toast.makeText(getContext(), "Tonton video sampai selesai untuk mengun d u h tema", Toast.LENGTH_LONG).show();
                    }else {
                        // load web
                        loadWeb();
                    }

                    loadReward();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                }
            };
            rewardedAd.show(requireActivity(), onUserEarnedRewardListener);
        } else {

            tryToOpen += 1;
            if(tryToOpen == 3){
                // open web
                if (UnityAds.isReady(getString(R.string.unity_inter))) {
                    UnityAds.show(getActivity(), getString(R.string.unity_inter));
                }
                loadWeb();
            } else {
                Toast.makeText(getContext(), "Video sedang dimuat, tunggu beberapa saat atau 20 detik lagi", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void loadWeb(){

        Toast.makeText(getContext(), "Memuat halaman Un d u h", Toast.LENGTH_LONG).show();
        CustomTabHelper customTabHelper = new CustomTabHelper();

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getContext(), R.color.primary));
        // builder.addDefaultShareMenuItem();
        builder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = builder.build();
        String packageName = customTabHelper.getPackageNameToUse(getContext(), GBURL);
        if(packageName == null){
            /*Intent intentOpenUri = new Intent(getActivity(), WebViewActivity.class);
            intentOpenUri.putExtra(WebViewActivity.Companion.getEXTRA_URL(), Uri.parse(GBURL).toString());
            startActivity(intentOpenUri);*/
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GBURL));
            startActivity(browserIntent);
        } else {
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(getActivity(), Uri.parse(GBURL));
        }
    }

    private void loadReward() {
       RewardedAd.load(getContext(), getContext().getString(R.string.admob_reward), new AdRequest.Builder().build(),new RewardedAdLoadCallback(){
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error.
                rewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd mRewardedAd) {
                rewardedAd = mRewardedAd;
//                Log.d(TAG, "Ad was loaded.");
            }
        });

        if (UnityAds.isReady (getString(R.string.unity_inter))) {
            UnityAds.show (requireActivity(), getString(R.string.unity_inter));
        }
    }


}