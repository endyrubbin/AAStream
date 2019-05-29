package com.garage.aastream.handlers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.garage.aastream.interfaces.OnAppListLoadedCallback
import com.garage.aastream.models.AppItem
import java.io.File

/**
 * Created by Endy Rubbin on 23.05.2019 15:25.
 * For project: AAStream
 */
class AppHandler(
    val context: Context,
    private val preferences: PreferenceHandler) {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Load all apps installed on device
     */
    fun loadApps(callback: OnAppListLoadedCallback) {
        Thread(Runnable {
            packageManager.getInstalledApplications(0)?.let {
                val apps = ArrayList<AppItem>()
                it.forEach { info ->
                packageManager.getLaunchIntentForPackage(info.packageName)?.let {
                        apps.add(getAppItem(info))
                    }
                }
                if (apps.isNotEmpty()) {
                    callback.onAppListLoaded(apps)
                } else {
                    callback.onAppListLoadFailed()
                }
            }
        }).start()
    }

    /**
     * Gather app info
     *
     * @return the [AppItem] with set name and icon
     */
    @Suppress("DEPRECATION")
    private fun getAppItem(info: ApplicationInfo): AppItem {
        val file = File(info.sourceDir)
        val icon = if (info.icon != 0) "android.resource://" + info.packageName + "/" + info.icon else null
        val name = if (!file.exists()) {
            info.packageName
        } else {
            info.loadLabel(packageManager)
        }.toString()
        return AppItem(name, info.packageName, icon)
    }

    /**
     * @return all favored [AppItem]s
     */
    fun getFavorites(): ArrayList<AppItem> {
        val favorites = preferences.getFavorites()
        favorites.forEach { app -> app.favorite = false }
        return favorites
    }

    /**
     * Check if app is in favorites
     */
    fun isInFavorites(app: AppItem): Boolean {
        return getFavorites().firstOrNull { it.equalTo(app) }?.let { true } ?: false
    }
}