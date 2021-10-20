package com.whatsapp.chattema.extensions.fragments

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.whatsapp.chattema.data.Preferences
import com.whatsapp.chattema.extensions.context.preferences
import com.whatsapp.chattema.ui.activities.base.BaseThemedActivity

inline fun <reified T : View> Fragment.findView(
    @IdRes id: Int,
    logException: Boolean = false
): Lazy<T?> {
    return lazy {
        try {
            view?.findViewById<T>(id)
        } catch (e: Exception) {
            if (logException) e.printStackTrace()
            null
        }
    }
}

fun Fragment.string(@StringRes resId: Int, vararg formatArgs: Any? = arrayOf()): String =
    try {
        getString(resId, *formatArgs.map { it.toString() }.toTypedArray())
    } catch (e: Exception) {
        ""
    }

val Fragment.preferences: Preferences
    get() = (activity as? BaseThemedActivity<*>)?.preferences ?: requireContext().preferences