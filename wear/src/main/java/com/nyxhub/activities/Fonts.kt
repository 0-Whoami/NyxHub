package com.nyxhub.activities

import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.nyxhub.base.Card
import com.nyxhub.base.Heading
import com.nyxhub.base.LazyListWrapper
import com.nyxhub.base.Loading
import com.nyxhub.base.NotifyingAnimation
import com.nyxhub.base.SwappableCard
import com.nyxhub.base.Text
import com.nyxhub.base.VerticalDivider
import com.nyxhub.base.primary_color
import com.nyxhub.network.LazyInterface
import com.nyxhub.network.NetworkLazy
import com.nyxhub.network.apiUrl
import com.nyxhub.network.download_url
import com.nyxhub.network.getJsonData
import com.nyxhub.network.name
import com.nyxhub.network.type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nyx.constants.Constant
import java.io.File
import java.io.InputStream

class Fonts : ComponentActivity() {
    private val fontFile = File(Constant.EXTRA_FONT)
    private fun getCurrentFont() : FontFamily {
        val typeface = try {
            Typeface.createFromFile(fontFile)
        } catch (e : Exception) {
            null
        }
        return if (typeface != null) FontFamily(typeface) else FontFamily.Monospace
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        val fonts = mutableListOf<fontItem>()
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            getJsonData("$apiUrl/fonts") {
                if (it.getString(type) == "file") fonts.add(fontItem(it.getString(name), it.getString(download_url), cacheDir))
            }
        }
        setContent {
            val composeScope = rememberCoroutineScope()
            var currentFont by remember { mutableStateOf(getCurrentFont()) }
            val columnState = rememberScalingLazyListState()
            var anim by remember { mutableStateOf(false) }
            LazyListWrapper(columnState = columnState) {
                item { Text() }
                item { Heading("FONTS") }
                item {
                    SwappableCard(deleteroot = { fontFile.delete() }) {
                        Box(modifier = Modifier
                            .padding(5.dp)
                            .border(1.dp, primary_color)) {
                            Text("CURRENT FONT", fontFamily = currentFont, modifier = Modifier
                                .fillParentMaxWidth()
                                .height(50.dp)
                                .wrapContentHeight())
                            NotifyingAnimation(enable = anim, onFinished = { anim = false })
                        }
                    }
                }
                itemsIndexed(fonts) { index, item ->
                    NetworkLazy(coroutineScope, item) {
                        VerticalDivider()
                        Card(item.name, {
                            if (item.loadable != null) {
                                composeScope.launch {
                                    columnState.animateScrollToItem(2)
                                    anim = true
                                }
                                coroutineScope.launch {
                                    item.file.copyTo(fontFile, true)
                                    currentFont = item.loadable!!
                                }
                            }
                        }, fontFam = item.loadable, focused = index == it - 1, endingContent = { if (item.loadable == null) Loading() })
                    }
                }
            }
        }
    }

    @Stable
    internal class fontItem(name : String, url : String, cacheDir : File) : LazyInterface<FontFamily>(name, url) {
        val file = File(cacheDir, name)
        override fun decodeFromStream(inp : InputStream) {
            file.delete()
            inp.copyTo(file.outputStream())
            loadable = FontFamily(Typeface.createFromFile(file))
        }

        override fun onError() {
            loadable = FontFamily.Monospace
        }

    }


}