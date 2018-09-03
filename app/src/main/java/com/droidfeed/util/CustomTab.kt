package com.droidfeed.util

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.URLUtil
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import com.droidfeed.R
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject

class CustomTab @Inject constructor(val activity: Activity) {

    companion object {
        private const val CHROME_STABLE_PACKAGE = "com.android.chrome"
    }

    /**
     * Opens the given URL in custom tab.
     *
     * @param url
     */
    fun showTab(url: String) {
        if (URLUtil.isValidUrl(url)) {
            if (isPackageAvailable(CHROME_STABLE_PACKAGE)) {
                bindCustomTabsService(url, CHROME_STABLE_PACKAGE)
            } else {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(activity, Uri.parse(url))
            }
        } else {
            Snackbar.make(
                activity.window.decorView,
                R.string.error_invalid_article_url,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun bindCustomTabsService(url: String, chromePackage: String) {
        val connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                client: CustomTabsClient
            ) {
                launchCustomTab(client, url)
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }

        CustomTabsClient.bindCustomTabsService(activity, chromePackage, connection)
    }

    private fun launchCustomTab(
        client: CustomTabsClient,
        url: String
    ) {
        client.warmup(0L)
        val builder = CustomTabsIntent.Builder()

        builder.apply {
            setToolbarColor(
                ContextCompat.getColor(
                    activity,
                    android.R.color.white
                )
            )

            setStartAnimations(
                activity,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            setExitAnimations(
                activity,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

        val customTabsIntent = builder.build()

        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }

    /**
     * Checks if the given package name is available on the device.
     *
     * @param targetPackage package name to be checked
     * @return true if available
     */
    private fun isPackageAvailable(targetPackage: String): Boolean {
        return try {
            activity.packageManager.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}