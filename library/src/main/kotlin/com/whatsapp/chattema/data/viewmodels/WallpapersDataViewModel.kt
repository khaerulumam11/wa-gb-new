package com.whatsapp.chattema.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.whatsapp.chattema.data.db.FramesDatabase
import com.whatsapp.chattema.data.models.Collection
import com.whatsapp.chattema.data.models.Favorite
import com.whatsapp.chattema.data.models.Wallpaper
import com.whatsapp.chattema.data.network.WallpapersJSONService
import com.whatsapp.chattema.extensions.context.isNetworkAvailable
import com.whatsapp.chattema.extensions.resources.hasContent
import com.whatsapp.chattema.extensions.utils.context
import com.whatsapp.chattema.extensions.utils.lazyMutableLiveData
import com.whatsapp.chattema.extensions.utils.postDelayed
import com.whatsapp.chattema.extensions.utils.tryToObserve
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Suppress("unused", "RemoveExplicitTypeArguments")
open class WallpapersDataViewModel(application: Application) : AndroidViewModel(application) {

    private val wallpapersData: MutableLiveData<List<Wallpaper>> by lazyMutableLiveData()
    val wallpapers: List<Wallpaper>
        get() = wallpapersData.value.orEmpty()

    private val collectionsData: MutableLiveData<ArrayList<Collection>> by lazyMutableLiveData()
    val collections: ArrayList<Collection>
        get() = ArrayList(collectionsData.value.orEmpty())

    private val favoritesData: MutableLiveData<List<Wallpaper>> by lazyMutableLiveData()
    val favorites: List<Wallpaper>
        get() = favoritesData.value.orEmpty()

    internal var whenReady: (() -> Unit)? = null

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(WallpapersJSONService::class.java)
    }

    open fun internalTransformWallpapersToCollections(wallpapers: List<Wallpaper>): List<Collection> {
        val collections =
            wallpapers.joinToString(",") { it.collections ?: "" }
                .replace("|", ",")
                .split(",")
                .distinct()
        val importantCollectionsNames = listOf(
            "all", "featured", "new", "wallpaper of the day", "wallpaper of the week"
        )
        val sortedCollectionsNames =
            listOf(importantCollectionsNames, collections).flatten().distinct()

        var usedCovers = ArrayList<String>()
        val actualCollections: ArrayList<Collection> = ArrayList()
        sortedCollectionsNames.forEach { collectionName ->
            val collection = Collection(collectionName)
            wallpapers.filter { it.collections.orEmpty().contains(collectionName, true) }
                .distinctBy { it.url }
                .forEach { collection.push(it) }
            usedCovers = collection.setupCover(usedCovers)
            if (collection.count > 0) actualCollections.add(collection)
        }
        return actualCollections
    }

    private suspend fun transformWallpapersToCollections(wallpapers: List<Wallpaper>): ArrayList<Collection> =
        withContext(IO) { ArrayList(internalTransformWallpapersToCollections(wallpapers)) }

    private suspend fun getWallpapersFromDatabase(): List<Wallpaper> =
        withContext(IO) {
            try {
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.getAllWallpapers()
                    .orEmpty()
            } catch (e: Exception) {
                arrayListOf<Wallpaper>()
            }
        }

    private suspend fun deleteAllWallpapers() =
        withContext(IO) {
            try {
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.nuke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private suspend fun saveWallpapers(wallpapers: List<Wallpaper>) =
        withContext(IO) {
            try {
                deleteAllWallpapers()
                delay(10)
                FramesDatabase.getAppDatabase(context)?.wallpapersDao()?.insertAll(wallpapers)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun internalAddToLocalFavorites(wallpapers: List<Wallpaper>): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()
            ?.insertAll(wallpapers.map { Favorite(it.url) })
        return true
    }

    private fun internalNukeAllLocalFavorites(): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.nuke()
        return true
    }

    open suspend fun internalGetFavorites(): List<Favorite> =
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.getAllFavorites().orEmpty()

    open suspend fun internalAddToFavorites(wallpaper: Wallpaper): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.insert(Favorite(wallpaper.url))
        return true
    }

    open suspend fun internalRemoveFromFavorites(wallpaper: Wallpaper): Boolean {
        FramesDatabase.getAppDatabase(context)?.favoritesDao()?.delete(Favorite(wallpaper.url))
        return true
    }

    suspend fun addAllToLocalFavorites(wallpapers: List<Wallpaper>): Boolean =
        withContext(IO) {
            try {
                internalAddToLocalFavorites(wallpapers)
            } catch (e: Exception) {
                false
            }
        }

    suspend fun nukeLocalFavorites(): Boolean =
        withContext(IO) {
            try {
                internalNukeAllLocalFavorites()
            } catch (e: Exception) {
                false
            }
        }

    private suspend fun getFavorites(): List<Favorite> =
        withContext(IO) {
            val result = try {
                internalGetFavorites()
            } catch (e: Exception) {
                listOf<Favorite>()
            }
            result
        }

    private suspend fun safeAddToFavorites(wallpaper: Wallpaper): Boolean =
        withContext(IO) {
            try {
                internalAddToFavorites(wallpaper)
            } catch (e: Exception) {
                false
            }
        }

    private suspend fun safeRemoveFromFavorites(wallpaper: Wallpaper): Boolean =
        withContext(IO) {
            try {
                internalRemoveFromFavorites(wallpaper)
            } catch (e: Exception) {
                false
            }
        }

    private suspend fun handleWallpapersData(
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        newWallpapers: List<Wallpaper> = listOf(),
        force: Boolean = false
    ) {
        val localWallpapers = try {
            getWallpapersFromDatabase()
        } catch (e: Exception) {
            arrayListOf<Wallpaper>()
        }

        val filteredWallpapers = if (newWallpapers.isNotEmpty()) {
            newWallpapers.filter { it.url.hasContent() }.distinctBy { it.url }
        } else localWallpapers

        val favorites = if (loadFavorites) getFavorites() else ArrayList()
        val actualNewWallpapers =
            filteredWallpapers.map { wall ->
                wall.apply {
                    this.isInFavorites = favorites.any { fav -> fav.url == wall.url }
                }
            }

        if (loadCollections) {
            val newCollections = transformWallpapersToCollections(actualNewWallpapers)
            val areTheSameCollections = areTheSameLists(collections, newCollections)
            if (!areTheSameCollections || collections.isNullOrEmpty() || force)
                postCollections(newCollections)
        }

        val areTheSameWallpapers = areTheSameLists(localWallpapers, actualNewWallpapers)
        if (!areTheSameWallpapers || wallpapers.isNullOrEmpty() || force)
            postWallpapers(actualNewWallpapers)

        if (loadFavorites) {
            val actualFavorites =
                actualNewWallpapers.filter { wllppr -> favorites.any { fav -> fav.url == wllppr.url } }
            val areTheSameFavorites = areTheSameLists(favorites, actualFavorites)
            if (!areTheSameFavorites || favorites.isNullOrEmpty() || force)
                postFavorites(actualFavorites)
        }
        saveWallpapers(actualNewWallpapers)
        postDelayed(10) { whenReady?.invoke() }
    }

    private suspend fun loadRemoteData(
        url: String = "",
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        force: Boolean = false
    ) {
        if (!context.isNetworkAvailable() || !url.hasContent()) return
        try {
            val remoteWallpapers = service.getJSON(url)
            handleWallpapersData(loadCollections, loadFavorites, remoteWallpapers, force)
        } catch (e: Exception) {
        }
    }

    fun loadData(
        url: String = "",
        loadCollections: Boolean = true,
        loadFavorites: Boolean = true,
        force: Boolean = false
    ) {
        viewModelScope.launch {
            delay(10)
            handleWallpapersData(loadCollections, loadFavorites, listOf(), force)
            loadRemoteData(url, loadCollections, loadFavorites, force)
        }
    }

    fun addToFavorites(wallpaper: Wallpaper): Boolean {
        var success = false
        viewModelScope.launch {
            success = safeAddToFavorites(wallpaper)
            delay(10)
            loadData("", loadCollections = false, loadFavorites = true, force = true)
        }
        return success
    }

    fun removeFromFavorites(wallpaper: Wallpaper): Boolean {
        var success = false
        viewModelScope.launch {
            success = safeRemoveFromFavorites(wallpaper)
            delay(10)
            loadData("", loadCollections = false, loadFavorites = true, force = true)
        }
        return success
    }

    private fun postWallpapers(result: List<Wallpaper>) {
        wallpapersData.value = null
        wallpapersData.postValue(result)
    }

    private fun postCollections(result: ArrayList<Collection>) {
        collectionsData.value = null
        collectionsData.postValue(result)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun postFavorites(result: List<Wallpaper>) {
        favoritesData.value = null
        favoritesData.postValue(result)
    }

    fun observeWallpapers(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        wallpapersData.tryToObserve(owner, onUpdated)
    }

    fun observeCollections(owner: LifecycleOwner, onUpdated: (ArrayList<Collection>) -> Unit) {
        collectionsData.tryToObserve(owner, onUpdated)
    }

    fun observeFavorites(owner: LifecycleOwner, onUpdated: (List<Wallpaper>) -> Unit) {
        favoritesData.tryToObserve(owner, onUpdated)
    }

    fun destroy(owner: LifecycleOwner) {
        wallpapersData.removeObservers(owner)
        collectionsData.removeObservers(owner)
        favoritesData.removeObservers(owner)
    }

    private fun <T> areTheSameLists(local: List<T>, remote: List<T>): Boolean {
        try {
            var areTheSame = true
            for ((index, wallpaper) in remote.withIndex()) {
                if (local.indexOf(wallpaper) != index) {
                    areTheSame = false
                    break
                }
            }
            if (!areTheSame) return false
            val difference = ArrayList<T>(remote).apply { removeAll(local) }.size
            return difference <= 0 && remote.size == local.size
        } catch (e: Exception) {
            return false
        }
    }
}
