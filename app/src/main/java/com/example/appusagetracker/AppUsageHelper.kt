package com.example.appusagetracker

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log.e
import androidx.core.content.ContextCompat.getSystemService
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.apply


data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val lastTimeUsed: Long,
    val totalTimeInForeground: Long
)

fun getAppUsageStats(context:Context):List<AppUsageInfo>{
    val usageStatsManager = context
        .getSystemService(Context
            .USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startTime = calendar.timeInMillis
    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        endTime
    )
    val pm =context.packageManager

    val launchIntent = Intent(Intent.ACTION_MAIN,null).apply {
       addCategory(Intent.CATEGORY_LAUNCHER)
   }
    val launcherApps = context.packageManager.queryIntentActivities(
        launchIntent,
        0
    ).map {  it.activityInfo.applicationInfo.packageName}.toSet()



    val combinedStats =  stats
        .filter { it.packageName in launcherApps}
        .groupBy { it.packageName }
        .map{(packageName,usageStatsList)->
           val appName = try {
               val appInfo = pm.getApplicationInfo(packageName,0)
               pm.getApplicationLabel(appInfo).toString()
           }catch (e: Exception){
               e.printStackTrace()
               packageName
           }
            val totalTime = usageStatsList.sumOf { it.totalTimeInForeground}
            val lastTimeUsed = usageStatsList.maxOfOrNull { it.lastTimeUsed }?:0
            AppUsageInfo(
                packageName = packageName,
                appName = appName,
                lastTimeUsed = lastTimeUsed,
                totalTimeInForeground = totalTime
            )
        }
        .sortedByDescending { it.totalTimeInForeground }
    return combinedStats
}
 fun hasUsagePermission(context: Context):Boolean{
    val appOpsManager =
        context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOpsManager.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun requestUsageAccess(context:Context){
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}