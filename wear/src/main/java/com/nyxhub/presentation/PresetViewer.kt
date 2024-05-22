package com.nyxhub.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Download
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import com.nyxhub.nyx.NyxConstants
import com.nyxhub.nyx.NyxConstants.CONFIG_PATH
import com.nyxhub.presentation.ui.AnimatedVisibility
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.FailedScreen
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.presentation.ui.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

const val skippingCaution =
    "Caution: Skipping the bootstrap installation will prevent you from using any Termux packages. It is highly recommended to install the bootstrap if you have not done so already. Click again to confirm skipping the installation."
const val installCaution =
    "Caution: Proceeding with the installation may wipe existing data. Ensure that you have backed up any important data before continuing. Click again to confirm you understand and wish to proceed with the installation."

class Image(val name: String, val src: ImageBitmap)
class PresetViewer : ComponentActivity() {
    private lateinit var urlOfFlavour: String
    private var loading by mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private val result = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            scope.launch {
                loading = true
                getData("$urlOfFlavour/files") { jsonArray ->
                    phraseJsonArray(this@PresetViewer, jsonArray) {}
                }
                loading = false
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        urlOfFlavour = intent.getStringExtra("url")!!
        val prev = mutableStateListOf<Image>()
        var networkState by mutableStateOf(NetWorkResponse.Loading)
        var descriptions by mutableStateOf("")
        scope.launch {
            networkState = getData(urlOfFlavour) { jsonArray ->
                for (i in 0..<jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("name") == "ABOUT.md") {
                        async {
                            download(obj.getString("download_url")) { inp ->
                                val reader = BufferedReader(InputStreamReader(inp))
                                val response = StringBuilder()

                                var line: String?
                                while (reader.readLine().also { line = it } != null) {
                                    response.append(line)
                                }
                                reader.close()
                                descriptions = response.toString()
                            }
                        }
                    }
                    if (obj.getString("name") == "res" && obj.getString(
                            "type"
                        ) == "dir"
                    ) {
                        networkState = NetWorkResponse.Loading
                        async {
                            networkState = getData(obj.getString("url")) { inJson ->
                                for (j in 0..<inJson.length()) {
                                    val obj2 = inJson.getJSONObject(j)
                                    download(obj2.getString("download_url")) { inp ->
                                        try {
                                            prev.add(
                                                Image(
                                                    obj2.getString("name").split(".")[0],
                                                    BitmapFactory.decodeStream(inp).asImageBitmap()
                                                )
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        setContent {
            val state =
                rememberPagerState { prev.size.plus(if (networkState == NetWorkResponse.Loading) 1 else 0) }
            val pageIndicatorState = remember {
                object : PageIndicatorState {
                    override val pageCount: Int
                        get() = state.pageCount
                    override val pageOffset: Float
                        get() = state.currentPageOffsetFraction
                    override val selectedPage: Int
                        get() = state.currentPage
                }
            }
            LazyList(
                blur = loading, contentPadding = PaddingValues(0.dp)
            ) {
                item { Text(text = "Preview", fontFamily = font1) }
                item {
                    if (networkState == NetWorkResponse.Failed) FailedScreen()
                    else {
                        var showName by remember { mutableStateOf(false) }
                        HorizontalPager(state = state) {
                            if (it == prev.size) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) { Loading() }
                            } else {
                                val textWidth = rememberTextMeasurer()
                                Image(bitmap = prev[it].src,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clickable { showName = !showName }
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .drawWithCache {
                                            val measure = textWidth.measure(
                                                prev[it].name, style = TextStyle(
                                                    color = primary_color, fontFamily = font1
                                                )
                                            )
                                            onDrawWithContent {
                                                drawContent()
                                                if (showName) {
                                                    drawRect(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color.Transparent, Color.Black
                                                            )
                                                        )
                                                    )
                                                    drawText(
                                                        textLayoutResult = measure,
                                                        topLeft = Offset(
                                                            (size.width - measure.size.width) / 2,
                                                            size.height - (measure.size.height + 20)
                                                        )
                                                    )
                                                }
                                            }
                                        })


                            }
                        }
                        HorizontalPageIndicator(pageIndicatorState)

                    }
                }
                item {
                    Button(
                        modifier = Modifier
                            .background(
                                primary_color, RoundedCornerShape(50)
                            )
                            .padding(10.dp),
                        color = surfaceColor,
                        icon = Icons.TwoTone.Download,
                        text = "Install"
                    ) {
                        result.launch(
                            Intent(
                                this@PresetViewer, BootStrapChooser::class.java
                            )
                        )
                    }
                }
                item {
                    Text(
                        text = descriptions,
                        fontFamily = font1,
                        color = primary_color.copy(alpha = 0.7f)
                    )
                }

            }
            AnimatedVisibility(visible = loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Loading()
                }
            }

        }
    }

}


private val knownFiles = listOf(
    EXTRA_CONFIG_NAME,
    EXTRA_NORMAL_BACKGROUND_NAME,
    EXTRA_BLUR_BACKGROUND_NAME,
    FONT_FILE_NAME,
    KEYS_FILE_NAME,
    COLOR_FILE_NAME
)


const val scriptPath = "${NyxConstants.TERMUX_HOME_DIR_PATH}/temp/main.sh"


fun phraseJsonArray(context: Context, jsonArray: JSONArray, onFail: () -> Unit) {
    var launchScript = false

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        when (val name = obj.getString("name")) {
            in knownFiles -> download(
                obj.getString("download_url"), onFail
            ) { it.copyTo(File("$CONFIG_PATH/$name").outputStream()) }

            "login" -> download(
                obj.getString("download_url"), onFail
            ) { it.copyTo(File("${NyxConstants.TERMUX_BIN_PREFIX_DIR_PATH}/login").outputStream()) }

            "motd" -> download(
                obj.getString("download_url"), onFail
            ) { it.copyTo(File("${NyxConstants.TERMUX_PREFIX_DIR_PATH}/etc/motd").outputStream()) }

            else -> {
                val file = File("${NyxConstants.TERMUX_HOME_DIR_PATH}/temp")
                if (name == "main.sh") launchScript = true
                recursiveDownload(file, obj, onFail)
            }
        }
    }
    if (launchScript) startNyx(context, "bash $scriptPath\n")

}

private fun recursiveDownload(parent: File, obj: JSONObject, onFail: () -> Unit) {
    if (!parent.exists()) parent.mkdirs()
    if (obj.getString("type") == "dir") {
        getData(obj.getString("url")) {
            for (i in 0 until it.length()) recursiveDownload(
                File(parent, obj.getString("name")), it.getJSONObject(i), onFail
            )
        }
    } else download(obj.getString("download_url"), onFail) {
        it.copyTo(File(parent, obj.getString("name")).outputStream())
    }
}