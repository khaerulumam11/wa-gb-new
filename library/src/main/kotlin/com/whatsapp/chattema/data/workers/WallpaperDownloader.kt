package com.whatsapp.chattema.data.workers

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.whatsapp.chattema.R
import com.whatsapp.chattema.data.network.DownloadListenerThread
import com.whatsapp.chattema.data.network.MediaScanner
import com.whatsapp.chattema.extensions.context.preferences
import com.whatsapp.chattema.extensions.context.string
import com.whatsapp.chattema.extensions.context.toast
import com.whatsapp.chattema.extensions.frames.filenameAndExtension
import com.whatsapp.chattema.extensions.resources.createIfDidNotExist
import com.whatsapp.chattema.extensions.resources.hasContent
import kotlinx.coroutines.coroutineScope
import java.io.File

class WallpaperDownloader(context: Context, params: WorkerParameters) :
    ContextAwareWorker(context, params),
    DownloadListenerThread.DownloadListener {

    @Suppress("DEPRECATION")
    private fun downloadUsingNotificationManager(url: String, file: File): Long {
        val fileUri: Uri? = Uri.fromFile(file)
        fileUri ?: return -1L

        val request = DownloadManager.Request(Uri.parse(url))
            .apply {
                setTitle(file.name)
                setDescription(context?.string(R.string.downloading_wallpaper, file.name))
                setDestinationUri(fileUri)
                setAllowedOverRoaming(false)
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE
                            or DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                allowScanningByMediaScanner()
            }

        val downloadManager: DownloadManager? =
            context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        downloadManager ?: return -1L

        val downloadId = try {
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            return -1L
        }

        val thread =
            DownloadListenerThread(
                context, downloadManager, downloadId, file.absolutePath, this
            )
        thread.start()
        return downloadId
    }

    override suspend fun doWork(): Result = coroutineScope {
        val url: String = inputData.getString(DOWNLOAD_URL_KEY) ?: ""
        if (!url.hasContent()) return@coroutineScope Result.failure()

        val (filename, extension) = url.filenameAndExtension
        val folder = context?.preferences?.downloadsFolder
            ?: context?.externalCacheDir ?: context?.cacheDir
        val filePath = "$folder${File.separator}$filename$extension"

        val file = File(filePath)
        if (file.exists() && file.length() > 0L) {
            onSuccess(filePath)
            val outputData = workDataOf(
                DOWNLOAD_PATH_KEY to file.absolutePath,
                DOWNLOAD_FILE_EXISTED to true
            )
            return@coroutineScope Result.success(outputData)
        }

        file.parentFile?.createIfDidNotExist()
        file.delete()

        val downloadId = downloadUsingNotificationManager(url, file)
        if (downloadId == -1L) return@coroutineScope Result.failure()

        val outputData = workDataOf(
            DOWNLOAD_PATH_KEY to filePath,
            DOWNLOAD_TASK_KEY to downloadId,
            DOWNLOAD_FILE_EXISTED to false
        )
        return@coroutineScope Result.success(outputData)
    }

    override fun onSuccess(path: String) {
        super.onSuccess(path)
        MediaScanner.scan(context, path)
    }

    override fun onFailure(exception: Exception) {
        super.onFailure(exception)
        try {
            context?.toast(exception.message ?: "Unexpected error!", Toast.LENGTH_LONG)
        } catch (e: Exception) {
        }
    }

    companion object {
        internal const val DOWNLOAD_PATH_KEY = "download_path"
        internal const val DOWNLOAD_URL_KEY = "download_url"
        internal const val DOWNLOAD_TASK_KEY = "download_task"
        internal const val DOWNLOAD_FILE_EXISTED = "download_file_existed"

        fun buildRequest(url: String): OneTimeWorkRequest? {
            if (!url.hasContent()) return null
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(DOWNLOAD_URL_KEY to url)
            return OneTimeWorkRequest.Builder(WallpaperDownloader::class.java)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
        }
    }
}