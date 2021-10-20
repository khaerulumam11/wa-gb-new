package com.whatsapp.chattema;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

public class ThemesFragment extends Fragment {

    public ThemesFragment() {
        // Required empty public constructor
    }

    private Button mUnduhButton, mCaraUnduhButton, mCaraPasangButton, mVideoPanduanButton;

    public static ThemesFragment newInstance(String param1, String param2) {
        ThemesFragment fragment = new ThemesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_themes, container, false);
        mUnduhButton = rootView.findViewById(R.id.unduh);
        mCaraUnduhButton = rootView.findViewById(R.id.cara_unduh);
        mCaraPasangButton = rootView.findViewById(R.id.cara_pasang);
        mVideoPanduanButton = rootView.findViewById(R.id.video_panduan);

        mUnduhButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), UnduhActivity.class)));

        mCaraUnduhButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), CaraUnduhActivity.class)));

        mCaraPasangButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), CaraPasangActivity.class)));

        mVideoPanduanButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), PanduanActivity.class)));

        AdLoader adLoader = new AdLoader.Builder(getContext(), getContext().getString(R.string.admob_native))
                .forUnifiedNativeAd(unifiedNativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();

                    TemplateView template = rootView.findViewById(R.id.my_template);
                    template.setStyles(styles);
                    template.setNativeAd(unifiedNativeAd);

                })
                .build();

        adLoader.loadAd(new Utils().getAdsRequest(getActivity()));
        UnityAds.initialize(requireActivity(), getString(R.string.unity_app_id), new IUnityAdsListener() {
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
        return rootView;
    }
}