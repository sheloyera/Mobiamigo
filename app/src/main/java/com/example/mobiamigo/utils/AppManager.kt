package com.example.mobiamigo.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.example.mobiamigo.data.AppItem

object AppManager {
    fun getInstalledApps(context: Context): List<AppItem> {
        val packageManager: PackageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos: List<ResolveInfo> =
            packageManager.queryIntentActivities(mainIntent, 0)

        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                AppItem(
                    label = info.loadLabel(packageManager).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(packageManager),
                    resolveInfo = info
                )
            }
            .sortedBy { it.label }
    }
    fun launchApp(context: Context, appItem: AppItem) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(appItem.packageName)

        if (intent != null) {
            context.startActivity(intent)
        }
    }
}