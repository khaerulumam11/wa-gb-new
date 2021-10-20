package com.whatsapp.chattema;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PanduanActivity extends AppCompatActivity {

    PanduanAdapter panduanAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panduan);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView emptyText = findViewById(R.id.text_empty);

        LinearLayout adHolder = findViewById(R.id.adHolder);

        AdView adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId(getString(R.string.admob_banner));
        adHolder.addView(adView);
        adView.loadAd(new Utils().getAdsRequest(this));
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



        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        List<PanduanAdapter.PanduanModel> models = new ArrayList<>();
        AndroidNetworking.get("https://raw.githubusercontent.com/colddrygame/g-b-w-a/main/panduan.json")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.dismiss();
                        for(int i = 0; i<response.length(); i++){
                            try {
                                JSONObject object = response.getJSONObject(i);
                                PanduanAdapter.PanduanModel model = new PanduanAdapter.PanduanModel(
                                        object.getString("title"), object.getString("url")
                                );
                                models.add(model);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        panduanAdapter = new PanduanAdapter(PanduanActivity.this, models);
                        recyclerView.setAdapter(panduanAdapter);
                        panduanAdapter.setClickListener((view, position, url) -> {
                            // item
                            Log.e("err", "send notif");
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(browserIntent);
                        });

                        if(models.size() == 0){
                            emptyText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        emptyText.setVisibility(View.VISIBLE);
                        progressDialog.dismiss();
                    }
                });
    }
}