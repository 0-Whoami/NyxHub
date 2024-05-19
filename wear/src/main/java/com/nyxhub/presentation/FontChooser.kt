package com.nyxhub.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.nyxhub.nyx.FileChooser
import com.nyxhub.nyx.key
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.presentation.ui.Loading
import com.nyxhub.nyx.NyxConstants.CONFIG_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

const val demo =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ\nabcdefghijklmnopqrstuvwxyz\n0123456789\n~!@#$%^&*()_+`-={}[]|\\:\";'<>?,./'"

class FontData(
    val name: String,
    var path: String,
    var typeface: MutableState<FontFamily?> = mutableStateOf(null)
)
const val FONT_FILE_NAME="font.ttf"
const val ITALIC_FONT_FILE_NAME="italic_$FONT_FILE_NAME"
class FontChooser : ComponentActivity() {
    val file = File("$CONFIG_PATH/$FONT_FILE_NAME")
    private var currentFont: MutableState<FontFamily?> =
        mutableStateOf(FontFamily(Typeface.MONOSPACE))
    private val customFonts = mutableStateListOf<FontData>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private val result = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            coroutineScope.launch {
                try {
                    val data = it.data?.getStringExtra(key)!!
                    val font = FontFamily(Typeface.createFromFile(data))
                    customFonts.add(FontData(
                        File(data).nameWithoutExtension, data
                    ).apply {
                        typeface.value = font
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadFonts() {
        try {
            currentFont.value = FontFamily(Typeface.createFromFile(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        cacheDir.deleteRecursively()
        loadFonts()
        var netWorkResponse by mutableStateOf(NetWorkResponse.Loading)
        coroutineScope.launch {
            getData("$apiUrl/fonts") {
                for (i in 0..<it.length()) {
                    val obj = it.getJSONObject(i)
                    customFonts.add(
                        FontData(
                            obj.getString("name").let { name -> name.slice(0..<name.length - 4) },
                            obj.getString("download_url")
                        )
                    )
                }
                netWorkResponse = NetWorkResponse.Success
            }
        }
        super.onCreate(savedInstanceState)
        setContent {
            LazyList {
                item {
                    Text(text = "Font Chooser", fontFamily = font1, color = Color.White)
                }
                item {
                    FontPreview(it = FontData("Current", "", currentFont), Icons.TwoTone.Delete) {
                        file.delete()
                    }
                }
                item { }
                items(customFonts) {
                    FontPreview(it = it)
                }
                if (netWorkResponse == NetWorkResponse.Loading) item { Loading() }
                item {
                    Button(icon = Icons.TwoTone.Add, text = "Add") {
                        result.launch(
                            Intent(
                                this@FontChooser, FileChooser::class.java
                            ).putStringArrayListExtra(
                                "filters", arrayListOf("ttf")
                            )
                        )
                    }
                }
            }
        }

    }
    @Composable
    fun FontPreview(
        it: FontData, icon: ImageVector = Icons.TwoTone.Download, onIconClick: () -> Unit = {
            file.delete()
            File(it.path).copyTo(file)
        }
    ) {
        var show by remember { mutableStateOf(false) }
        Column(modifier = Modifier
            .clickable { show = !show }
            .fillMaxWidth()
            .background(
                surfaceColor, RoundedCornerShape(if (show) 10 else 25)
            )
            .padding(15.dp)
            .animateContentSize(),
            verticalArrangement = Arrangement.SpaceEvenly) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.weight(2f),
                    text = it.name,
                    fontFamily = it.typeface.value,
                    color = Color.White,
                    style = MaterialTheme.typography.title2
                )
                if (it.typeface.value != null) Icon(imageVector = icon,
                    contentDescription = null,
                    tint = surfaceColor,
                    modifier = Modifier
                        .clickable {
                            onIconClick()
                            loadFonts()
                        }
                        .size(45.dp)
                        .background(
                            Color.White, CircleShape
                        )
                        .padding(5.dp))
                else Loading()
            }
            if (show) Text(
                text = demo,
                fontFamily = it.typeface.value,
                color = Color.White,
                style = MaterialTheme.typography.body2
            )
        }
        LaunchedEffect(key1 = Unit) {
            if (it.typeface.value == null) {
                coroutineScope.launch {
                    download(it.path, {}) { input ->
                        val f = File(cacheDir, "${it.name}.ttf")
                        f.delete()
                        input.copyTo(f.outputStream())
                        it.typeface.value = FontFamily(Typeface.createFromFile(f))
                        it.path = f.absolutePath
                    }
                }
            }
        }
    }
}



