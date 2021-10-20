package com.whatsapp.chattema;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PanduanAdapter extends RecyclerView.Adapter<PanduanAdapter.ViewHolder> {

    public static class PanduanModel{
        String title;
        String url;

        public PanduanModel(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private List<PanduanModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    PanduanAdapter(Context context, List<PanduanModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_panduan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PanduanModel panduanModel = mData.get(position);
        holder.mActionButton.setText(panduanModel.title);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button mActionButton;

        ViewHolder(View itemView) {
            super(itemView);
            mActionButton = itemView.findViewById(R.id.action_button);
            mActionButton.setOnClickListener(view -> mClickListener.onItemClick(view, getAdapterPosition(), getItem(getAdapterPosition()).url));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), getItem(getAdapterPosition()).url);
        }
    }

    PanduanModel getItem(int id) {
        return mData.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position, String url);
    }
}