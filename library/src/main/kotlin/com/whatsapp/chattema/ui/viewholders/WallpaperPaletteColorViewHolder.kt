package com.whatsapp.chattema.ui.viewholders

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.palette.graphics.Palette
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import com.whatsapp.chattema.R
import com.whatsapp.chattema.extensions.context.toast
import com.whatsapp.chattema.extensions.resources.toHexString
import com.whatsapp.chattema.extensions.utils.bestTextColor
import com.whatsapp.chattema.extensions.views.context
import com.whatsapp.chattema.extensions.views.findView

class WallpaperPaletteColorViewHolder(view: View) : SectionedViewHolder(view) {

    private val colorBtn: AppCompatButton? by view.findView(R.id.palette_color_btn)

    fun bind(swatch: Palette.Swatch? = null) {
        swatch ?: return
        colorBtn?.setBackgroundColor(swatch.rgb)
        colorBtn?.setTextColor(swatch.bestTextColor)
        colorBtn?.text = swatch.rgb.toHexString()
        colorBtn?.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(
                ClipData.newPlainText("label", swatch.rgb.toHexString())
            )
            context.toast(R.string.copied_to_clipboard)
        }
    }
}