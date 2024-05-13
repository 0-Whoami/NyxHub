package com.nyxhub.file

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBackIosNew
import androidx.compose.material.icons.twotone.FileOpen
import androidx.compose.material.icons.twotone.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.nyxhub.presentation.font1
import com.nyxhub.presentation.surfaceColor
import com.termux.nyxhub.R
import java.io.File
import java.util.Locale
const val key="path"
class FileChooser : ComponentActivity() {
    private var dir: String by mutableStateOf(Environment.getExternalStorageDirectory().absolutePath)
    private lateinit var findTypes : ArrayList<String>

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findTypes= intent.getStringArrayListExtra("filters") ?: arrayListOf()
        setContent {
            fileChooser()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalHorologistApi::class)
    @Composable
    fun fileChooser() {
        val listDir = remember(dir) { File(dir).listFiles()?.sorted()?.filter { it.isDirectory } }
        val listImage = remember(dir) {
            File(dir).listFiles()?.sorted()?.filter {
                if (findTypes.isEmpty()) !it.isDirectory
                else
                it.extension.lowercase(Locale.ENGLISH) in findTypes
            }
        }
        val state = rememberScalingLazyListState()
        ScalingLazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = state,
            modifier = Modifier
                .rotaryWithScroll(state)
        ) {
            item { Text(text = dir, fontFamily = font1, color = Color.White) }
            items(listDir ?: listOf()) {
                Row(modifier = Modifier
                    .fillParentMaxWidth()
                    .clickable { dir = it.absolutePath }
                    .background(surfaceColor, RoundedCornerShape(50))
                    .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(
                        imageVector = Icons.TwoTone.Folder,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(text = it.name, fontFamily = font1, color = Color.White)
                }
            }
            items(listImage ?: listOf()) {
                val imageBmp = remember {
                    try{
                        BitmapFactory.decodeFile(
                            it.absolutePath,
                            BitmapFactory.Options().apply { inSampleSize = 4 }).asImageBitmap()
                    }catch (ex: Exception){
                        null
                    }
                }
                Row(modifier = Modifier
                    .fillParentMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(50))
                    .padding(10.dp)
                    .clickable {
                        setResult(RESULT_OK, Intent().apply { putExtra(key, it.absolutePath) })
                        finish()
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) { if(imageBmp!=null)
                    Image(
                        bitmap = imageBmp,
                        contentDescription = null,
                        Modifier
                            .clip(CircleShape)
                            .size(25.dp),
                        contentScale = ContentScale.Crop
                    )
                    else
                    Icon(
                        imageVector = Icons.TwoTone.FileOpen,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(text = it.name, fontFamily = font1, color = Color.White)
                }
            }
            if (isNotRootDir()) item {
                Row(modifier = Modifier
                    .fillParentMaxWidth()
                    .clickable { goBackToPreviousDir() }
                    .background(surfaceColor, RoundedCornerShape(50))
                    .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(
                        imageVector = Icons.TwoTone.ArrowBackIosNew,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(text = "Back", fontFamily = font1, color = Color.White)
                }
            }

        }
    }

    private fun isNotRootDir(): Boolean =
        dir != Environment.getExternalStorageDirectory().absolutePath

    private fun goBackToPreviousDir() {
        dir = File(dir).parent
    }

    override fun onBackPressed() {
        if (isNotRootDir())
            goBackToPreviousDir()
        else
            super.onBackPressed()
    }

}


