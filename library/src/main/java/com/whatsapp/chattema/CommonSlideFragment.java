package com.whatsapp.chattema;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonSlideFragment extends Fragment {

    private static final String ARG_TITLE = "param_title";
    private static final String ARG_DESCRIPTION = "param_description";
    private static final String ARG_DRAWABLE = "param_drawable";
    private static final String ARG_SHOW_PANDUAN = "param.show.panduan";

    private String mParamTitle;
    private String mParamDescription;
    private int mParamDrawable;
    private boolean mShowPanduan = false;

    private TextView mTitle, mDescription;
    private ImageView mImageView;
    private Button mActionButton;

    public CommonSlideFragment() {

    }

    public static CommonSlideFragment newInstance(String title, String description, int drawable, boolean... mShowPanduan) {
        CommonSlideFragment fragment = new CommonSlideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_DRAWABLE, drawable);
        if(mShowPanduan != null){
            args.putBoolean(ARG_SHOW_PANDUAN, (mShowPanduan.length >= 1) && mShowPanduan[0]);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamTitle = getArguments().getString(ARG_TITLE);
            mParamDescription = getArguments().getString(ARG_DESCRIPTION);
            mParamDrawable = getArguments().getInt(ARG_DRAWABLE);
            mShowPanduan = getArguments().getBoolean(ARG_SHOW_PANDUAN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_common_slide, container, false);
        mTitle = rootView.findViewById(R.id.text_title);
        mDescription = rootView.findViewById(R.id.text_description);
        mImageView = rootView.findViewById(R.id.image);
        mActionButton = rootView.findViewById(R.id.action_button);

        mTitle.setText(
                (mParamTitle != null) ? mParamTitle : ""
        );

        mDescription.setText(
                (mParamDescription != null) ? mParamDescription : ""
        );

        mImageView.setImageResource(mParamDrawable);

        if(mShowPanduan){
            mActionButton.setVisibility(View.VISIBLE);
            mActionButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), PanduanActivity.class)));
        }

        return rootView;
    }
}