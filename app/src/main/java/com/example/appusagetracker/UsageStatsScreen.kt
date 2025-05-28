package com.example.appusagetracker

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

@Composable
fun UsageStatsScreen(
    context:Context,
    modifier: Modifier = Modifier
){
    val usageStats =  remember {
        mutableStateListOf<AppUsageInfo>()
    }
    LaunchedEffect(Unit) {
        if(!hasUsagePermission(context)){
            requestUsageAccess(context)
        }else{
           val data = getAppUsageStats(context)
            usageStats.clear()
            usageStats.addAll(data)
        }
    }
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        itemsIndexed(usageStats){index,app->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.elevatedCardElevation()
            ){
                Column(
                    modifier = Modifier.padding(12.dp)
                ){
                    Text(text="${index+1}")
                    Text(text = app.appName, style = MaterialTheme.typography.titleMedium)
                    Text(text="Package: ${app.packageName}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text(text="Last Time Used: ${DateUtils.getRelativeTimeSpanString(app.lastTimeUsed)}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Screen time: ${TimeUnit.MILLISECONDS.toMinutes(app.totalTimeInForeground)} mins",
                        style = MaterialTheme.typography.bodyMedium)

                }

            }
        }

    }

}