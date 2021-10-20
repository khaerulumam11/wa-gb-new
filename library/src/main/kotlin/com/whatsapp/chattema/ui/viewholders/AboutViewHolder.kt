package com.whatsapp.chattema.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.whatsapp.chattema.R
import com.whatsapp.chattema.data.models.AboutItem
import com.whatsapp.chattema.extensions.resources.hasContent
import com.whatsapp.chattema.extensions.views.findView
import com.whatsapp.chattema.extensions.views.loadFramesPic
import com.whatsapp.chattema.extensions.views.visibleIf
import com.whatsapp.chattema.ui.widgets.AboutButtonsLayout

class AboutViewHolder(view: View) : SectionedViewHolder(view) {

    private val photoImageView: AppCompatImageView? by view.findView(R.id.photo)
    private val nameTextView: TextView? by view.findView(R.id.name)
    private val descriptionTextView: TextView? by view.findView(R.id.description)
    private val buttonsView: AboutButtonsLayout? by view.findView(R.id.buttons)

    fun bind(aboutItem: AboutItem?) {
        aboutItem ?: return
        nameTextView?.text = aboutItem.name
        nameTextView?.visibleIf(aboutItem.name.hasContent())
        descriptionTextView?.text = aboutItem.description
        descriptionTextView?.visibleIf(aboutItem.description.orEmpty().hasContent())
        aboutItem.links.forEach { buttonsView?.addButton(it.first, it.second) }
        buttonsView?.visibleIf(aboutItem.links.isNotEmpty())
        photoImageView?.loadFramesPic(aboutItem.photoUrl.orEmpty(), cropAsCircle = true)
    }
}