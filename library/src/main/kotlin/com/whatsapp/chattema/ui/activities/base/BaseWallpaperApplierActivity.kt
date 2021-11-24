package com.whatsapp.chattema.ui.activities.base

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.material.snackbar.Snackbar
import com.whatsapp.chattema.R
import com.whatsapp.chattema.Utils
import com.whatsapp.chattema.data.Preferences
import com.whatsapp.chattema.data.workers.WallpaperApplier
import com.whatsapp.chattema.data.workers.WallpaperApplier.Companion.APPLY_EXTERNAL_KEY
import com.whatsapp.chattema.data.workers.WallpaperApplier.Companion.APPLY_OPTION_KEY
import com.whatsapp.chattema.data.workers.WallpaperDownloader.Companion.DOWNLOAD_PATH_KEY
import com.whatsapp.chattema.extensions.context.string
import com.whatsapp.chattema.extensions.resources.getUri
import com.whatsapp.chattema.extensions.views.snackbar
import java.io.File
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback




@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseWallpaperApplierActivity<out P : Preferences> :
    BaseWallpaperFetcherActivity<P>() {
    var adRequest: AdRequest = AdRequest.Builder().build()
    fun startApplyThumb(applyOption: Int) {
        cancelWorkManagerTasks()
        val newApplyTask = WallpaperApplier.buildRequest(wallPaperThumbUrl, applyOption)
        newApplyTask?.let { task ->
            workManager.enqueue(newApplyTask)
            workManager.getWorkInfoByIdLiveData(task.id)
                .observe(this, Observer { info ->
                    if (info != null) {
                        if (info.state.isFinished) {
                            if (info.state == WorkInfo.State.SUCCEEDED) {
                                val option = info.outputData.getInt(APPLY_OPTION_KEY, -1)
                                if (option == APPLY_EXTERNAL_KEY) {
                                    onWallpaperReadyToBeApplied(
                                        info.outputData.getString(DOWNLOAD_PATH_KEY) ?: ""
                                    )
                                } else onWallpaperApplied()
                            } else if (info.state == WorkInfo.State.FAILED) {
                                onDownloadError()
                            }
                        } else if (info.state == WorkInfo.State.ENQUEUED) {
                            onWallpaperApplicationEnqueued(applyOption)
                        }
                    }
                })
        }
    }


    fun startApply(applyOption: Int) {

       InterstitialAd.load(this, getString(R.string.admob_intersitial), Utils().getAdsRequest(this),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(@NonNull interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                    // Handle the error
                    mInterstitialAd = null
                }
            })

        cancelWorkManagerTasks()
        val newApplyTask = WallpaperApplier.buildRequest(wallpaperDownloadUrl, applyOption)
        newApplyTask?.let { task ->
            workManager.enqueue(newApplyTask)
            workManager.getWorkInfoByIdLiveData(task.id)
                .observe(this, Observer { info ->
                    if (info != null) {
                        if (info.state.isFinished) {
                            if (info.state == WorkInfo.State.SUCCEEDED) {
                                val option = info.outputData.getInt(APPLY_OPTION_KEY, -1)
                                if (option == APPLY_EXTERNAL_KEY) {
                                    onWallpaperReadyToBeApplied(
                                        info.outputData.getString(DOWNLOAD_PATH_KEY) ?: ""
                                    )
                                } else onWallpaperApplied()
                            } else if (info.state == WorkInfo.State.FAILED) {
                                Log.e("Error", "FUCCCCK")
                                startApplyThumb(applyOption)
                                //onDownloadError()
                            }
                        } else if (info.state == WorkInfo.State.ENQUEUED) {
                            onWallpaperApplicationEnqueued(applyOption)
                        }
                    }
                })
        }
    }

    private fun onWallpaperApplicationEnqueued(applyOption: Int) {
        try {
            currentSnackbar = snackbar(
                if (applyOption == APPLY_EXTERNAL_KEY) R.string.applying_preparing
                else R.string.applying_wallpaper_def,
                Snackbar.LENGTH_INDEFINITE,
                snackbarAnchorId
            )
        } catch (e: Exception) {
        }
    }

    private fun onWallpaperApplied() {
        try {
            if (mInterstitialAd!=null) {
                mInterstitialAd!!.show(this)
            }
            currentSnackbar = snackbar(R.string.applying_applied, anchorViewId = snackbarAnchorId)
            Toast.makeText(this, R.string.applying_applied, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
        }
        cancelWorkManagerTasks()
    }

    open fun onWallpaperReadyToBeApplied(path: String) {
        currentSnackbar?.dismiss()
        val file = File(path)
        val fileUri: Uri? = file.getUri(this) ?: Uri.fromFile(file)
        fileUri?.let {
            val setWall = Intent(Intent.ACTION_ATTACH_DATA)
            setWall.setDataAndType(it, "image/*")
            setWall.putExtra("mimeType", "image/*")
            setWall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivityForResult(
                    Intent.createChooser(setWall, string(R.string.apply_w_external_app)),
                    APPLY_WITH_OTHER_APP_CODE
                )
            } catch (e: Exception) {
                onDownloadError()
            }
        } ?: { onDownloadError() }()
        cancelWorkManagerTasks()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APPLY_WITH_OTHER_APP_CODE) {
            if (resultCode != 0) onWallpaperApplied()
            else onDownloadError()
        }
    }

    companion object {
        private const val APPLY_WITH_OTHER_APP_CODE = 575
    }
}