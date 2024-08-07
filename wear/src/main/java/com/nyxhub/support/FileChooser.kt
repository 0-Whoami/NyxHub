package com.nyxhub.support

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.foundation.lazy.itemsIndexed
import com.nyxhub.base.Card
import com.nyxhub.base.Heading
import com.nyxhub.base.LazyListWrapper
import com.nyxhub.base.Text
import com.nyxhub.base.VerticalDivider
import java.io.File

const val key = "path"

class FileChooser : ComponentActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var dir by remember { mutableStateOf(Environment.getExternalStorageDirectory().absolutePath) }
            val fileList = remember(dir) { File(dir).listFiles() ?: arrayOf() }
            LazyListWrapper {
                item { Text() }
                item { Heading(dir) }
                item {
                    VerticalDivider()
                    Card("..", { dir = File(dir).parent ?: dir }, it == 0)
                }
                itemsIndexed(fileList) { i, file ->
                    Card(file.name, {
                        if (file.isDirectory) dir = file.absolutePath else {
                            setResult(RESULT_OK, intent.putExtra(key, file.absolutePath))
                            finish()
                        }
                    }, focused = i + 1 == it)
                }
            }
        }
    }

    //    @Composable    fun MainUi() {
    //        val listDir = remember(dir) { File(dir).listFiles()?.sorted()?.filter { it.isDirectory } }
    //        val listImage = remember(dir) {
    //            File(dir).listFiles()?.sorted()?.filter {
    //                if (findTypes.isEmpty()) !it.isDirectory
    //                else it.extension.lowercase(Locale.ENGLISH) in findTypes
    //            }
    //        }
    //        LazyList {
    //            item { Text(text = dir, fontFamily = font1, color = Color.White) }
    //            items(listDir ?: listOf()) {
    //                Row(modifier = Modifier
    //                    .clickable { dir = it.absolutePath }
    //                    .fillParentMaxWidth()
    //                    .background(surfaceColor, RoundedCornerShape(50))
    //                    .padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
    //                    Icon(imageVector = Icons.TwoTone.Folder, contentDescription = null, tint = Color.White)
    //                    Text(text = it.name, fontFamily = font1, color = Color.White)
    //                }
    //            }
    //            items(listImage ?: listOf()) {
    //                val imageBmp = remember {
    //                    try {
    //                        BitmapFactory.decodeFile(it.absolutePath, BitmapFactory.Options().apply { inSampleSize = 4 }).asImageBitmap()
    //                    } catch (ex : Exception) {
    //                        null
    //                    }
    //                }
    //                Row(modifier = Modifier
    //                    .clickable {
    //                        setResult(RESULT_OK, Intent().apply { putExtra(key, it.absolutePath) })
    //                        finish()
    //                    }
    //                    .fillParentMaxWidth()
    //                    .background(surfaceColor, RoundedCornerShape(50))
    //                    .padding(10.dp),
    //                    verticalAlignment = Alignment.CenterVertically,
    //                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
    //                    if (imageBmp != null) Image(bitmap = imageBmp,
    //                                                contentDescription = null,
    //                                                Modifier
    //                                                    .clip(CircleShape)
    //                                                    .size(25.dp),
    //                                                contentScale = ContentScale.Crop)
    //                    else Icon(imageVector = Icons.TwoTone.FileOpen, contentDescription = null, tint = Color.White)
    //                    Text(text = it.name, fontFamily = font1, color = Color.White)
    //                }
    //            }
    //            if (isNotRootDir()) item {
    //                Row(modifier = Modifier
    //                    .clickable { goBackToPreviousDir() }
    //                    .fillParentMaxWidth()
    //                    .background(surfaceColor, RoundedCornerShape(50))
    //                    .padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
    //                    Icon(imageVector = Icons.TwoTone.ArrowBackIosNew, contentDescription = null, tint = Color.White)
    //                    Text(text = "Back", fontFamily = font1, color = Color.White)
    //                }
    //            }
    //
    //        }
    //    }
}


