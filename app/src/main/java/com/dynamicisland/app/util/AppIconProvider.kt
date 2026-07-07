package com.dynamicisland.app.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * Obtiene iconos y nombres de apps instaladas, cacheando en memoria
 * para evitar consultas repetidas al PackageManager (coste de CPU/IO).
 */
class AppIconProvider(context: Context) {

    private val packageManager: PackageManager = context.applicationContext.packageManager
    private val iconCache = LruCache<String, Drawable>(40)
    private val nameCache = LruCache<String, String>(40)

    fun getIcon(packageName: String): Drawable? {
        iconCache.get(packageName)?.let { return it }
        return try {
            val drawable = packageManager.getApplicationIcon(packageName)
            iconCache.put(packageName, drawable)
            drawable
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun getAppName(packageName: String): String {
        nameCache.get(packageName)?.let { return it }
        return try {
            val info: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(info).toString()
            nameCache.put(packageName, label)
            label
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /** Lista de apps instaladas con icono lanzable, para la pantalla de selección. */
    fun getLaunchableApps(): List<Pair<String, String>> {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName to getAppName(it.activityInfo.packageName) }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }
    }
}
