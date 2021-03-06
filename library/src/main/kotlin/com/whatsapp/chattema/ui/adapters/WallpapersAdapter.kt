package com.whatsapp.chattema.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.whatsapp.chattema.R
import com.whatsapp.chattema.data.models.Wallpaper
import com.whatsapp.chattema.extensions.views.inflate
import com.whatsapp.chattema.ui.viewholders.WallpaperViewHolder

internal class WallpapersAdapter(
    private val canShowFavoritesButton: Boolean = true,
    var canModifyFavorites: Boolean = true,
    var onClick: (Wallpaper, WallpaperViewHolder) -> Unit = { _, _ -> },
    var onFavClick: (Boolean, Wallpaper) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<WallpaperViewHolder>() {

    var wallpapers: ArrayList<Wallpaper> = ArrayList()
        set(value) {
            wallpapers.clear()
            wallpapers.addAll(value)
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(
            wallpapers[position],
            canShowFavoritesButton,
            canModifyFavorites,
            onClick,
            onFavClick
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder =
        WallpaperViewHolder(parent.inflate(R.layout.item_wallpaper))

    override fun getItemCount(): Int = wallpapers.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position
}