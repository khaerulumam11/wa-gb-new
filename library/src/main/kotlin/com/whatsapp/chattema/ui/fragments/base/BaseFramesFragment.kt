package com.whatsapp.chattema.ui.fragments.base

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.whatsapp.chattema.R
import com.whatsapp.chattema.extensions.context.resolveColor
import com.whatsapp.chattema.extensions.resources.hasContent
import com.whatsapp.chattema.extensions.resources.lighten
import com.whatsapp.chattema.extensions.resources.tint
import com.whatsapp.chattema.extensions.utils.SafeHandler
import com.whatsapp.chattema.extensions.utils.postDelayed
import com.whatsapp.chattema.extensions.views.attachSwipeRefreshLayout
import com.whatsapp.chattema.extensions.views.setPaddingBottom
import com.whatsapp.chattema.ui.activities.base.BaseSystemUIVisibilityActivity
import com.whatsapp.chattema.ui.widgets.StatefulRecyclerView

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseFramesFragment<T> : Fragment(R.layout.fragment_stateful_recyclerview),
    StatefulRecyclerView.StateDrawableModifier {

    private val originalItems: ArrayList<T> = ArrayList()
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    var recyclerView: StatefulRecyclerView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupContentBottomOffset()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView?.allowFirstRunCheck = allowCheckingFirstRun()
        setupContentBottomOffset(view)
        recyclerView?.stateDrawableModifier = this

        recyclerView?.emptyText = getEmptyText()
        recyclerView?.emptyDrawable = getEmptyDrawable()

        recyclerView?.noSearchResultsText = getNoSearchResultsText()
        recyclerView?.noSearchResultsDrawable = getNoSearchResultsDrawable()

        recyclerView?.loadingText = getLoadingText()

        recyclerView?.itemAnimator = DefaultItemAnimator()
        swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh)
        swipeRefreshLayout?.setOnRefreshListener { startRefreshing() }
        swipeRefreshLayout?.setColorSchemeColors(
            context?.resolveColor(R.attr.colorSecondary, 0) ?: 0
        )
        swipeRefreshLayout?.setProgressBackgroundColorSchemeColor(
            (context?.resolveColor(R.attr.colorSurface, 0) ?: 0).lighten(.1F)
        )
        recyclerView?.attachSwipeRefreshLayout(swipeRefreshLayout)
    }

    @Deprecated(
        "Deprecated in favor of setupContentBottomOffset",
        replaceWith = ReplaceWith("setupContentBottomOffset")
    )
    open fun setupRecyclerViewMargin(view: View? = null) {
        setupContentBottomOffset(view)
    }

    open fun setupContentBottomOffset(view: View? = null) {
        (view ?: getView())?.let { v ->
            v.post {
                v.setPaddingBottom(
                    (activity as? BaseSystemUIVisibilityActivity<*>)?.bottomNavigation?.measuredHeight
                        ?: 0
                )
            }
        }
    }

    open fun clearContentBottomOffset(view: View? = null) {
        (view ?: getView())?.let { it.post { it.setPaddingBottom(0) } }
    }

    internal fun setRefreshEnabled(enabled: Boolean) {
        swipeRefreshLayout?.isEnabled = enabled
    }

    internal fun applyFilter(filter: String, closed: Boolean) {
        if (closed) setupContentBottomOffset() else clearContentBottomOffset()
        recyclerView?.searching = filter.hasContent() && !closed
        updateItemsInAdapter(
            if (filter.hasContent() && !closed)
                getFilteredItems(ArrayList(originalItems), filter)
            else originalItems
        )
        if (!closed) scrollToTop()
    }

    private fun startRefreshing() {
        swipeRefreshLayout?.isRefreshing = true
        recyclerView?.loading = true
        try {
            loadData()
            postDelayed(500) { stopRefreshing() }
        } catch (e: Exception) {
            stopRefreshing()
        }
    }

    abstract fun loadData()

    internal fun stopRefreshing() {
        SafeHandler().post {
            swipeRefreshLayout?.isRefreshing = false
            recyclerView?.loading = false
        }
    }

    internal fun scrollToTop() {
        recyclerView?.post { recyclerView?.smoothScrollToPosition(0) }
    }

    fun updateItems(newItems: ArrayList<T>, stillLoading: Boolean = false) {
        this.originalItems.clear()
        this.originalItems.addAll(newItems)
        updateItemsInAdapter(newItems)
        if (!stillLoading) stopRefreshing()
    }

    override fun modifyDrawable(drawable: Drawable?): Drawable? =
        try {
            drawable?.tint(context?.resolveColor(R.attr.colorOnSurface, 0) ?: 0)
        } catch (e: Exception) {
            drawable
        }

    abstract fun getFilteredItems(originalItems: ArrayList<T>, filter: String): ArrayList<T>
    abstract fun updateItemsInAdapter(items: ArrayList<T>)
    open fun getTargetActivityIntent(): Intent? = null

    @StringRes
    open fun getLoadingText(): Int = R.string.loading

    @StringRes
    open fun getEmptyText(): Int = R.string.nothing_found

    @StringRes
    open fun getNoSearchResultsText(): Int = R.string.no_results_found

    @DrawableRes
    open fun getEmptyDrawable(): Int = R.drawable.ic_empty_section

    @DrawableRes
    open fun getNoSearchResultsDrawable(): Int = R.drawable.ic_empty_results

    open fun allowCheckingFirstRun(): Boolean = false
}
