package com.whatsapp.chattema.extensions.frames

import com.whatsapp.chattema.data.models.Wallpaper
import com.whatsapp.chattema.ui.adapters.WallpapersAdapter
import com.whatsapp.chattema.ui.viewholders.WallpaperViewHolder

internal fun wallpapersAdapter(
    canShowFavoritesButton: Boolean = true,
    canModifyFavorites: Boolean = true,
    block: WallpapersAdapter.() -> Unit
): WallpapersAdapter =
    WallpapersAdapter(canShowFavoritesButton, canModifyFavorites).apply(block)

internal fun WallpapersAdapter.onClick(what: (Wallpaper, WallpaperViewHolder) -> Unit) {
    this.onClick = what
}

internal fun WallpapersAdapter.onFavClick(what: (Boolean, Wallpaper) -> Unit) {
    this.onFavClick = what
}