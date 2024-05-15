package com.nyxhub.presentation

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.nyxhub.presentation.ui.FailedScreen
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.presentation.ui.Loading
import com.termux.nyxhub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class NetWorkResponse { Loading, Failed, Success }
class Presets : ComponentActivity() {

    class RemoteFile(
        val name: String,
        val url: String,
        var icon: MutableState<ImageBitmap?> = mutableStateOf(null)
    )
    private val scope = CoroutineScope(Dispatchers.IO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listOfNYXOptions = mutableListOf<RemoteFile>()
        val listOfGUIOptions = mutableListOf<RemoteFile>()
        var state: NetWorkResponse by mutableStateOf(NetWorkResponse.Loading)
        var state2: NetWorkResponse by mutableStateOf(NetWorkResponse.Loading)
        setContent {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                var firstpage by remember { mutableStateOf(true) }
                val animation by animateFloatAsState(
                    targetValue = if (firstpage) 0f else 1f, label = ""
                )
                val scalingState = rememberScalingLazyListState()
                Pages(
                    if (firstpage) state else state2,
                    if (firstpage) listOfNYXOptions else listOfGUIOptions,
                    scalingState
                )

                LaunchedEffect(key1 = firstpage) {
                    if (firstpage) {
                        if (listOfNYXOptions.isEmpty()) {
                            scope.launch {
                                state=getData(
                                    "$apiUrl/flavours/Nyx"
                                ) {
                                    for (i in 0..<it.length()) {
                                        val obj = it.getJSONObject(i)
                                        if (obj.getString("type") == "dir") listOfNYXOptions.add(
                                            RemoteFile(
                                                obj.getString(
                                                    "name"
                                                ), obj.getString("url").removeSuffix("?ref=main")
                                            )
                                        )
                                    }
                                }

                            }
                        }
                    } else {
                        if (listOfGUIOptions.isEmpty()) {
                            scope.launch {
                                state2=getData(
                                    "$apiUrl/flavours/GUI"
                                ) {
                                    for (i in 0..<it.length()) {
                                        val obj = it.getJSONObject(i)
                                        if (obj.getString("type") == "dir") listOfGUIOptions.add(
                                            RemoteFile(
                                                obj.getString(
                                                    "name"
                                                ), obj.getString("url")
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                    visible = !scalingState.isScrollInProgress && (if (firstpage)state else state2) != NetWorkResponse.Loading,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-10).dp)) {
                    val animator = (1 - 0.2f * sin(3.14f * animation))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .background(
                                surfaceColor, RoundedCornerShape(50)
                            )
                            .padding(5.dp)
                            .drawWithContent {
                                drawContent()
                                drawRoundRect(
                                    primary_color,
                                    cornerRadius = CornerRadius(size.height / 2, size.height / 2),
                                    size = size.copy(width = size.width / 2) * animator,
                                    blendMode = BlendMode.Exclusion,
                                    topLeft = Offset(
                                        size.width / 2 * animation, size.height * (1 - animator) / 2
                                    )
                                )
                            }
                            .padding(5.dp)) {
                        Text(
                            text = "CLI",
                            fontFamily = font1,
                            modifier = Modifier
                                .clickable { firstpage = true }
                                .weight(1f),
                            color = primary_color,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "GUI",
                            fontFamily = font1,
                            modifier = Modifier
                                .clickable { firstpage = false }
                                .weight(1f),
                            color = primary_color,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }

        }
    }

    @Composable
    fun Pages(
        state: NetWorkResponse,
        listOfNYXOptions: List<RemoteFile>,
        listState: ScalingLazyListState
    ) {
        when (state) {
            NetWorkResponse.Loading -> Loading()

            NetWorkResponse.Failed -> FailedScreen()

            NetWorkResponse.Success -> {
                LazyList(
                    state = listState,
                    anchorType = ScalingLazyListAnchorType.ItemStart
                ) { item { Text("Flavours") }
                    items(listOfNYXOptions) {
                        Catalogue(it = it)
                    }
                    item {  }
                }
            }
        }
    }

    @Composable
    fun Catalogue(it: RemoteFile) {
        Row(
            modifier = Modifier
                .clickable { startActivity(Intent(this@Presets, PresetViewer::class.java).putExtra("url",it.url))}
                .clip(RoundedCornerShape(25))
                .background(surfaceColor)
                .fillMaxWidth()
                .height(75.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(75.dp), contentAlignment = Alignment.Center
            ) {
                if (it.icon.value == null) Loading()
                else Image(
                    bitmap = it.icon.value!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = it.name, fontFamily = font1
            )
        }
        LaunchedEffect(Unit) {
            if (it.icon.value==null)
            scope.launch {
                val failure: () -> Unit = {
                    it.icon.value = ImageBitmap.imageResource(resources, R.drawable.network_error)
                }
                getData(it.url) { json ->
                    for (i in 0..<json.length()) {
                        val obj = json.getJSONObject(i)
                        if (obj.getString("name").startsWith("icon")) {
                            download(
                                obj.getString("download_url"), failure
                            ) { inp ->
                                it.icon.value = BitmapFactory.decodeStream(inp).asImageBitmap()
                            }
                            return@getData
                        }
                    }
                    failure()
                }
            }
        }
    }
}
