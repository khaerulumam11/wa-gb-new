package com.whatsapp.chattema.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.whatsapp.chattema.R
import com.whatsapp.chattema.ThemesFragment
import com.whatsapp.chattema.Utils
import com.whatsapp.chattema.data.Preferences
import com.whatsapp.chattema.data.models.Collection
import com.whatsapp.chattema.data.models.Wallpaper
import com.whatsapp.chattema.extensions.context.drawable
import com.whatsapp.chattema.extensions.context.findView
import com.whatsapp.chattema.extensions.context.getAppName
import com.whatsapp.chattema.extensions.context.string
import com.whatsapp.chattema.extensions.fragments.cancelable
import com.whatsapp.chattema.extensions.fragments.mdDialog
import com.whatsapp.chattema.extensions.fragments.message
import com.whatsapp.chattema.extensions.fragments.positiveButton
import com.whatsapp.chattema.extensions.fragments.title
import com.whatsapp.chattema.extensions.resources.hasContent
import com.whatsapp.chattema.ui.activities.base.BaseBillingActivity
import com.whatsapp.chattema.ui.fragments.CollectionsFragment
import com.whatsapp.chattema.ui.fragments.WallpapersFragment
import com.whatsapp.chattema.ui.fragments.base.BaseFramesFragment
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.util.Locale


@Suppress("LeakingThis", "MemberVisibilityCanBePrivate")
abstract class FramesActivity : BaseBillingActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    val adContainer: LinearLayout? by findView(R.id.ad_container)

    open val wallpapersFragment: WallpapersFragment? by lazy {
        WallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers), canModifyFavorites())
    }
    open val collectionsFragment: CollectionsFragment? by lazy {
        CollectionsFragment.create(ArrayList(wallpapersViewModel.collections))
    }
    open val favoritesFragment: WallpapersFragment? by lazy {
        WallpapersFragment.createForFavs(
            ArrayList(wallpapersViewModel.favorites),
            canModifyFavorites()
        )
    }

    var currentFragment: Fragment? = null
        private set

    open val initialFragmentTag: String = WallpapersFragment.TAG

    private var form: ConsentForm? = null

    private var currentTag: String = initialFragmentTag
    private var oldTag: String = initialFragmentTag

    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Utils().isNetworkAvailable(this)){
            initAndSetup()
            checkIsX()
        } else {
            this.mdDialog {
                title(R.string.app_theme)
                cancelable(false)
                message("No Internet or your internet connection is unstable, please try again later")
                positiveButton(android.R.string.ok) { dialog ->
                    dialog.dismiss()
                    this@FramesActivity.finish()
                }
            }.show()
        }
    }


    fun checkIsX(){
        progressDialog = ProgressDialog(this);
        progressDialog?.setCancelable(false);
        progressDialog?.setMessage("Loading...");

        progressDialog!!.show()
        AndroidNetworking.get("https://geoip-db.com/json/")
            .setPriority(Priority.HIGH)
            .doNotCacheResponse()
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("ee", response.toString())
                    progressDialog!!.dismiss()
                    try {
                        if (response.getString("country_code") == "US" || response.getString("country_code") == "SG" || response.getString(
                                "country_code"
                            ) == "CA") {
                            // todo: set false
                            decideTheme(false)
                        } else {
                            // todo: set true
                            decideTheme(true)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onError(anError: ANError) {
                    progressDialog!!.dismiss()
                    // todo: set true
                    decideTheme(true)
                    Toast.makeText(
                        this@FramesActivity,
                        "Connection error, please check internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }


    fun decideTheme(withTheme: Boolean){
        if(withTheme && Locale.getDefault().language.toUpperCase(Locale.ROOT) != "EN"){
            val menuItem = bottomNavigation?.menu?.add(0, R.id.theme, 700, "Themes")
            menuItem?.setIcon(R.drawable.ic_muzei_icon)
            bottomNavigation?.setSelectedItemId(R.id.theme)
        } else {
            bottomNavigation?.menu?.removeItem(R.id.theme)
        }

        getConsentStatus()

    }


    open fun getConsentStatus() {
        val consentInformation: ConsentInformation = ConsentInformation.getInstance(this)

        // ConsentInformation.getInstance(this).addTestDevice("44CD6F17ED2307DD1BF28DCAD46F35BD");
        // consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        val publisherIds = arrayOf(getString(R.string.admob_publisher_id))
        consentInformation.requestConsentInfoUpdate(
            publisherIds,
            object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                    // User's consent status successfully updated.
                    if (ConsentInformation.getInstance(baseContext)
                            .isRequestLocationInEeaOrUnknown()) {
                        Log.e("STATUS", consentStatus.name)
                        when (consentStatus) {
                            ConsentStatus.UNKNOWN -> displayConsentForm()
                            ConsentStatus.PERSONALIZED -> initializeAds(true)
                            ConsentStatus.NON_PERSONALIZED -> initializeAds(false)
                        }
                    } else {
                        Log.d("Consent", "Not in EU, displaying normal ads")
                        initializeAds(true)
                    }
                }

                override fun onFailedToUpdateConsentInfo(errorDescription: String?) {
                    // User's consent status failed to update.
                    Log.e("Consent", errorDescription!!)
                }
            })
    }

    open fun displayConsentForm() {
        var privacyUrl: URL? = null
        try {
            privacyUrl = URL("https://colddrygame.github.io/privacy-policy/gbwall")
        } catch (e: MalformedURLException) {
            Log.e("Consent", "Error processing privacy policy url", e)
        }
        form = ConsentForm.Builder(this, privacyUrl)
            .withListener(object : ConsentFormListener() {
                override fun onConsentFormLoaded() {
                    form?.show()
                }

                override fun onConsentFormOpened() {}

                override fun onConsentFormClosed(
                    consentStatus: ConsentStatus,
                    userPrefersAdFree: Boolean
                ) {
                    if (consentStatus == ConsentStatus.PERSONALIZED) {
                        val editor: SharedPreferences.Editor =
                            PreferenceManager.getDefaultSharedPreferences(
                                baseContext
                            ).edit()
                        editor.putBoolean("personalized", true)
                        editor.commit()
                        initializeAds(true)
                    } else {
                        val editor: SharedPreferences.Editor =
                            PreferenceManager.getDefaultSharedPreferences(
                                baseContext
                            ).edit()
                        editor.putBoolean("personalized", false)
                        editor.commit()
                        initializeAds(false)
                    }
                }

                override fun onConsentFormError(errorDescription: String) {
                    // Consent form error. This usually happens if the user is not in the EU.
                    Log.e(
                        "Consent",
                        "Error loading consent form: $errorDescription"
                    )
                }
            })
            .withPersonalizedAdsOption()
            .withNonPersonalizedAdsOption()
            .build()
        form?.load()
    }

    fun initializeAds(withConsent: Boolean){
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = getString(R.string.admob_banner)

        adContainer?.addView(adView)

        //val adRequest = AdRequest.Builder().build()
        adView.loadAd(Utils().getAdsRequest(this))
    }

    fun initAndSetup(){

        MobileAds.initialize(this) {}
        setContentView(getLayoutRes())


        setSupportActionBar(toolbar)
        changeFragment(initialItemId, force = true)

        bottomNavigation?.selectedItemId = initialItemId
        bottomNavigation?.setOnNavigationItemSelectedListener { changeFragment(it.itemId) }

        bottomNavigation?.menu?.removeItem(R.id.theme)

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment?.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this, ::handleCollectionsUpdate)
        loadWallpapersData(true)



    }

    override fun onBackPressed() {
        if (currentItemId != initialItemId) bottomNavigation?.selectedItemId = initialItemId
        else super.onBackPressed()
    }

    fun updateToolbarTitle(itemId: Int = currentItemId) {
        var logoSet = false
        if (shouldShowToolbarLogo(itemId)) {
            drawable(string(R.string.toolbar_logo))?.let {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                supportActionBar?.setLogo(it)
                logoSet = true
            }
        }
        if (!logoSet) {
            supportActionBar?.setLogo(null)
            supportActionBar?.title = getToolbarTitleForItem(itemId) ?: getAppName()
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    open fun shouldShowToolbarLogo(itemId: Int): Boolean = itemId == initialItemId

    @LayoutRes
    open fun getLayoutRes(): Int = R.layout.activity_fragments_bottom_navigation

    override fun onFavoritesUpdated(favorites: List<Wallpaper>) {
        super.onFavoritesUpdated(favorites)
        favoritesFragment?.updateItems(ArrayList(favorites))
    }

    override fun getMenuRes(): Int = R.menu.toolbar_menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://colddrygame.github.io/privacy-policy/gbwall")
                )
                startActivity(browserIntent)
            }
            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.donate -> showDonationsDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSearchHint(itemId: Int): String = when (itemId) {
        R.id.wallpapers -> string(R.string.search_wallpapers)
        R.id.collections -> string(R.string.search_collections)
        R.id.favorites -> string(R.string.search_favorites)
        else -> string(R.string.search_x)
    }

    open fun getToolbarTitleForItem(itemId: Int): String? =
        when (itemId) {
            R.id.collections -> string(R.string.collections)
            R.id.favorites -> string(R.string.favorites)
            else -> null
        }

    open fun getNextFragment(itemId: Int): Pair<Pair<String?, Fragment?>?, Boolean>? =
        when (itemId) {
            R.id.wallpapers -> Pair(Pair(WallpapersFragment.TAG, wallpapersFragment), true)
            R.id.collections -> Pair(Pair(CollectionsFragment.TAG, collectionsFragment), true)
            R.id.favorites -> Pair(Pair(WallpapersFragment.FAVS_TAG, favoritesFragment), true)
            R.id.theme -> Pair(
                Pair(
                    WallpapersFragment.THEMES_TAG, ThemesFragment.newInstance(
                        "Woah",
                        "Duh"
                    )
                ), true
            )
            else -> null
        }

    @Suppress("MemberVisibilityCanBePrivate")
    fun changeFragment(itemId: Int, force: Boolean = false, animate: Boolean = true): Boolean {
        if (currentItemId != itemId || force) {
            val next = getNextFragment(itemId)
            // Pair ( Pair ( fragmentTag, fragment ) , shouldShowItemAsSelected )
            val nextFragmentTag = next?.first?.first.orEmpty()
            if (!nextFragmentTag.hasContent()) return false
            val nextFragment = next?.first?.second
            val shouldSelectItem = next?.second == true
            return nextFragment?.let {
                if (shouldSelectItem) {
                    oldTag = currentTag
                    currentItemId = itemId
                    currentTag = nextFragmentTag
                    loadFragment(nextFragment, currentTag, force, animate)
                    updateToolbarTitle(itemId)
                }
                shouldSelectItem
            } ?: false
        }
        return false
    }

    private fun loadFragment(
        fragment: Fragment?,
        tag: String,
        force: Boolean = false,
        animate: Boolean = true
    ) {
        fragment ?: return
        if (currentFragment !== fragment || force) {
            replaceFragment(fragment, tag, animate = animate)
            currentFragment = fragment
            invalidateOptionsMenu()
            updateSearchHint()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(CURRENT_FRAGMENT_KEY, currentTag)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentTag = savedInstanceState.getString(CURRENT_FRAGMENT_KEY, currentTag) ?: currentTag
        changeFragment(currentItemId, true)
    }

    override fun internalDoSearch(filter: String, closed: Boolean) {
        super.internalDoSearch(filter, closed)
        (currentFragment as? BaseFramesFragment<*>)?.let {
            it.setRefreshEnabled(!filter.hasContent())
            it.applyFilter(filter, closed)
        }
    }

    open fun handleCollectionsUpdate(collections: ArrayList<Collection>) {
        collectionsFragment?.updateItems(collections)
    }

    companion object {
        private const val CURRENT_FRAGMENT_KEY = "current_fragment"
    }
}