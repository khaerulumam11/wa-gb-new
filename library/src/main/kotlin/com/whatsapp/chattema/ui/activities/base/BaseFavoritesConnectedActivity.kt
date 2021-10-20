package com.whatsapp.chattema.ui.activities.base

import android.os.Bundle
import com.whatsapp.chattema.R
import com.whatsapp.chattema.data.Preferences
import com.whatsapp.chattema.data.models.Wallpaper
import com.whatsapp.chattema.data.viewmodels.WallpapersDataViewModel
import com.whatsapp.chattema.extensions.context.string
import com.whatsapp.chattema.extensions.utils.lazyViewModel

abstract class BaseFavoritesConnectedActivity<out P : Preferences> :
    BaseSystemUIVisibilityActivity<P>() {

    open val wallpapersViewModel: WallpapersDataViewModel by lazyViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shouldLoadFavorites() && canShowFavoritesButton())
            wallpapersViewModel.observeFavorites(this, ::onFavoritesUpdated)
    }

    internal fun addToFavorites(wallpaper: Wallpaper): Boolean {
        if (!canShowFavoritesButton()) return false
        if (canModifyFavorites()) return wallpapersViewModel.addToFavorites(wallpaper)
        onFavoritesLocked()
        return false
    }

    internal fun removeFromFavorites(wallpaper: Wallpaper): Boolean {
        if (!canShowFavoritesButton()) return false
        if (canModifyFavorites()) return wallpapersViewModel.removeFromFavorites(wallpaper)
        onFavoritesLocked()
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        wallpapersViewModel.destroy(this)
    }

    internal fun loadWallpapersData(remote: Boolean = false) {
        wallpapersViewModel.loadData(
            if (remote) getDataUrl() else "",
            loadCollections = shouldLoadCollections(),
            loadFavorites = shouldLoadFavorites() && canShowFavoritesButton(),
            force = true
        )
    }

    open fun shouldLoadCollections(): Boolean = true
    open fun shouldLoadFavorites(): Boolean = true
    open fun canShowFavoritesButton(): Boolean = true
    open fun canModifyFavorites(): Boolean = true
    open fun onFavoritesLocked() {}
    open fun onFavoritesUpdated(favorites: List<Wallpaper>) {}
    open fun getDataUrl(): String = string(R.string.json_url)
}