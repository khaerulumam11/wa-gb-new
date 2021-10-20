package com.whatsapp.chattema.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.whatsapp.chattema.R
import com.whatsapp.chattema.extensions.context.string
import com.whatsapp.chattema.extensions.views.findView

class WallpaperDetailViewHolder(view: View) : SectionedViewHolder(view) {

    private val titleTextView: TextView? by view.findView(R.id.detail_title)
    private val descriptionTextView: TextView? by view.findView(R.id.detail_description)

    fun bind(pair: Pair<Int, String>?) {
        pair ?: return
        bind(pair.first, pair.second)
    }

    fun bind(@StringRes title: Int, description: String) {
        try {
            bind(itemView.context.string(title), description)
        } catch (e: Exception) {
        }
    }

    fun bind(title: String, description: String) {
        titleTextView?.text = title
        descriptionTextView?.text = description
    }
}