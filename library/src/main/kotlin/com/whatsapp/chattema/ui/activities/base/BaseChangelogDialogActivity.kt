package com.whatsapp.chattema.ui.activities.base

import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.whatsapp.chattema.R
import com.whatsapp.chattema.data.Preferences
import com.whatsapp.chattema.extensions.context.isUpdate
import com.whatsapp.chattema.ui.fragments.buildChangelogDialog

abstract class BaseChangelogDialogActivity<out P : Preferences> : BaseSearchableActivity<P>() {

    private val changelogDialog: AlertDialog? by lazy { buildChangelogDialog() }

    fun showChangelog(force: Boolean = false) {
        if (isUpdate || force) changelogDialog?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // if (item.itemId == R.id.changelog) showChangelog(true)
        if(item.itemId == R.id.changelog){
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6195678933971303034"))
            startActivity(browserIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            changelogDialog?.dismiss()
        } catch (e: Exception) {
        }
    }
}