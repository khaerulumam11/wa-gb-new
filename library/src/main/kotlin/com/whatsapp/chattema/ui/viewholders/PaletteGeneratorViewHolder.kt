package com.whatsapp.chattema.ui.viewholders

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.apitiphy.harmoniccolorextractor.HarmonicColorExtractor
import com.whatsapp.chattema.R
import com.whatsapp.chattema.extensions.context.boolean
import com.whatsapp.chattema.extensions.resources.asBitmap
import com.whatsapp.chattema.extensions.utils.bestTextColor
import com.whatsapp.chattema.extensions.views.context

abstract class PaletteGeneratorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal val shouldColorTiles: Boolean by lazy {
        context.boolean(R.bool.enable_colored_tiles)
    }

    internal val generatePalette: (drawable: Drawable?) -> Unit by lazy {
        val listener: ((drawable: Drawable?) -> Unit) = { drwb ->
            onDrawableReady(drwb)
            if (shouldColorTiles) {
                val bitmap = drwb?.asBitmap()
                HarmonicColorExtractor().Builder()
                    .setBitmap(bitmap)
                    .setBottomSide()
                    .colors.let { harmonic ->
                        doWithColors(harmonic.backgroundColor, harmonic.bestTextColor)
                    }
            }
        }
        listener
    }

    open fun onDrawableReady(drawable: Drawable?) {}
    abstract fun doWithColors(@ColorInt bgColor: Int, @ColorInt textColor: Int)
}