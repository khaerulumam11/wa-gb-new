package com.whatsapp.chattema.ui.activities.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.whatsapp.chattema.R
import com.whatsapp.chattema.data.Preferences
import com.whatsapp.chattema.data.models.Wallpaper
import com.whatsapp.chattema.data.workers.WallpaperDownloader
import com.whatsapp.chattema.data.workers.WallpaperDownloader.Companion.DOWNLOAD_FILE_EXISTED
import com.whatsapp.chattema.data.workers.WallpaperDownloader.Companion.DOWNLOAD_PATH_KEY
import com.whatsapp.chattema.extensions.context.toast
import com.whatsapp.chattema.extensions.resources.getUri
import com.whatsapp.chattema.extensions.resources.hasContent
import com.whatsapp.chattema.extensions.views.snackbar
import java.io.File
import java.net.URLConnection

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseWallpaperFetcherActivity<out P : Preferences> :
    BaseFavoritesConnectedActivity<P>() {

    internal val workManager: WorkManager by lazy { WorkManager.getInstance(this) }
    internal var wallpaperDownloadUrl: String = ""
    internal var wallPaperThumbUrl: String = ""

    internal fun initDownload(wallpaper: Wallpaper?) {
        wallpaperDownloadUrl = wallpaper?.url.orEmpty()
        wallPaperThumbUrl = wallpaper?.thumbnail.orEmpty()
    }

    internal fun startDownload() {
        cancelWorkManagerTasks()
        val newDownloadTask = WallpaperDownloader.buildRequest(wallpaperDownloadUrl)
        newDownloadTask?.let { task ->
            workManager.enqueue(newDownloadTask)
            workManager.getWorkInfoByIdLiveData(task.id)
                .observe(this, Observer { info ->
                    if (info != null && info.state.isFinished) {
                        if (info.state == WorkInfo.State.SUCCEEDED) {
                            val path = info.outputData.getString(DOWNLOAD_PATH_KEY) ?: ""
                            val existed = info.outputData.getBoolean(DOWNLOAD_FILE_EXISTED, false)
                            if (existed) onDownloadExistent(path)
                            else onDownloadQueued()
                        } else if (info.state == WorkInfo.State.FAILED) {
                            onDownloadError()
                        }
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelWorkManagerTasks()
    }

    fun cancelWorkManagerTasks() {
        workManager.cancelAllWork()
        workManager.pruneWork()
    }

    private fun onDownloadQueued() {
        try {
            currentSnackbar = snackbar(R.string.download_starting, anchorViewId = snackbarAnchorId)
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    private fun onDownloadExistent(path: String) {
        try {
            val file = File(path)
            val fileUri: Uri? = file.getUri(this) ?: Uri.fromFile(file)
            currentSnackbar =
                snackbar(R.string.downloaded_previously, Snackbar.LENGTH_LONG, snackbarAnchorId) {
                    fileUri?.let {
                        var mimeType = URLConnection.guessContentTypeFromName(file.name).orEmpty()
                        if (!mimeType.hasContent()) mimeType = "image/*"
                        setAction(R.string.open) {
                            try {
                                startActivity(Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    setDataAndType(fileUri, mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                })
                            } catch (e: Exception) {
                                toast(R.string.error)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    internal fun onDownloadError() {
        try {
            currentSnackbar =
                snackbar(R.string.unexpected_error_occurred, anchorViewId = snackbarAnchorId)
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(WALLPAPER_URL_KEY, wallpaperDownloadUrl)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        wallpaperDownloadUrl = savedInstanceState.getString(WALLPAPER_URL_KEY, "") ?: ""
    }

    companion object {
        private const val WALLPAPER_URL_KEY = "wallpaper_download_url"
    }
}