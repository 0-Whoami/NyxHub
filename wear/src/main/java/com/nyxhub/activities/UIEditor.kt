package com.nyxhub.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nyxhub.base.Card
import com.nyxhub.base.Heading
import com.nyxhub.base.LazyListWrapper
import com.nyxhub.base.Switch
import com.nyxhub.base.Text
import com.nyxhub.base.TextValue
import com.nyxhub.base.VerticalDivider
import com.nyxhub.base.cornerDotBorder
import com.nyxhub.data.Properties
import nyx.constants.Constant

class UIEditor : ComponentActivity() {
    private val properties = Properties(Constant.EXTRA_CONFIG)
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        val prop = properties.prop
        setContent {
            var enableBlur by remember {
                mutableStateOf(properties.getBoolean(Constant.KEY_BLUR_ENABLED, Constant.DEFAULT_BLUR_ENABLED))
            }
            var enableBorder by remember {
                mutableStateOf(properties.getBoolean(Constant.KEY_ENABLE_BORDER, Constant.DEFAULT_ENABLE_BORDER))
            }
            val transcriptRows = remember(prop) { properties.getInt(Constant.KEY_TRANSCRIPT_ROWS, Constant.DEFAULT_TRANSCRIPT_ROWS) }
            val cornerRadius = remember(prop) { properties.getInt(Constant.KEY_CORNER_RADIUS, Constant.DEFAULT_CORNER_RADIUS) }
            val fontSize = remember(prop) { properties.getInt(Constant.KEY_FONT_SIZE, Constant.DEFAULT_FONT_SIZE) }
            LazyListWrapper {
                item { Text() }
                item { Heading("UI") }
                item {
                    VerticalDivider()
                    Card(focused = it == 0, text = "BACKGROUND") {
                        val bitmap = remember {
                            BitmapFactory.decodeFile(Constant.EXTRA_NORMAL_BACKGROUND, BitmapFactory.Options().apply { inSampleSize = 8 })
                                ?.asImageBitmap()
                        }
                        if (bitmap != null) Image(bitmap,
                                                  contentDescription = null,
                                                  modifier = Modifier
                                                      .size(50.dp)
                                                      .padding(7.dp)
                                                      .cornerDotBorder()
                                                      .padding(3.dp),
                                                  contentScale = ContentScale.Crop)
                    }
                }
                item {
                    VerticalDivider()
                    Switch("BLUR", focused = it == 1, onClick = {
                        enableBlur = it
                        properties.update(Constant.KEY_ENABLE_BORDER, it)
                    }, check = enableBlur)
                }
                item {
                    VerticalDivider()
                    Switch("BORDER", focused = it == 2, onClick = {
                        enableBorder = it
                        properties.update(Constant.KEY_ENABLE_BORDER, it)
                    }, check = enableBorder)
                }
                item {
                    VerticalDivider()
                    TextValue(text = "CORNER RADIUS", value = "$cornerRadius", onClick = {
                        EntryEditor.editEntry(this@UIEditor, Constant.KEY_CORNER_RADIUS, cornerRadius, properties)
                    }, focused = it == 3)
                }
                item { Spacer(Modifier.height(10.dp)) }
                item {
                    VerticalDivider()
                    Card("FONT", { startActivity(Intent(this@UIEditor, Fonts::class.java)) }, focused = it == 5)
                }
                item {
                    VerticalDivider()
                    TextValue(text = "FONT SIZE", value = "$fontSize", focused = it == 6, onClick = {
                        EntryEditor.editEntry(this@UIEditor, Constant.KEY_FONT_SIZE, fontSize, properties)
                    })
                }
                item {
                    Text("MEMORY",
                         Modifier
                             .fillParentMaxWidth()
                             .padding(5.dp), textAlign = TextAlign.Start)
                }
                item {
                    VerticalDivider()
                    TextValue(text = "TRANSCRIPT", value = "$transcriptRows", focused = it == 8, onClick = {
                        EntryEditor.editEntry(this@UIEditor, Constant.KEY_TRANSCRIPT_ROWS, transcriptRows, properties)
                    })
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        properties.save()
    }
}