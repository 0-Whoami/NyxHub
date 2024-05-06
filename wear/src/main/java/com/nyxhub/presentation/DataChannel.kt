package com.nyxhub.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.nyxhub.presentation.ui.Loading
import java.io.File

class DataChannel : DataClient.OnDataChangedListener,ComponentActivity() {
    private var label by mutableStateOf("Waiting")
    private var listOfFilesToQuery: List<String>? = null
    private var exitOnFinish=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listOfFilesToQuery=intent.getStringArrayListExtra("files")
        exitOnFinish=intent.getBooleanExtra("finish",false)
        setContent {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Loading()
                Text(text = label, fontFamily = font1)
            }
        }
    }
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/files" }
            .forEach { event ->
                val listOfFiles =
                    DataMapItem.fromDataItem(event.dataItem).dataMap.getStringArray("files")
                        ?.filter {
                            with(listOfFilesToQuery) {
                                this ?: return@filter true
                                this.contains(
                                    it
                                )
                            }
                        }

                for (path in listOfFiles ?: return@forEach) {
                    saveFileFromAsset(
                        DataMapItem.fromDataItem(event.dataItem).dataMap.getAsset(path)
                            ?: return@forEach, path
                    )
                }
            }
        dataEvents.release()
        label="Finished"
        if (exitOnFinish) finish()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    private fun saveFileFromAsset(asset: Asset, path: String) {
        label = "Saving $path"
        val file = File(path)
        if (file.exists()) file.delete()
        if (!(file.parentFile?.exists() ?: return)) file.parentFile?.mkdirs()
        Wearable.getDataClient(this).getFdForAsset(asset).addOnCompleteListener { res ->
            res.result.inputStream.use { it.copyTo(file.outputStream()) }
        }
        label = "Waiting for files..."
    }
}