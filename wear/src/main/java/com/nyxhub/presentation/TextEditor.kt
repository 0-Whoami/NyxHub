package com.nyxhub.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Save
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import java.io.File

class TextEditor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val file = File(intent.getStringExtra("file")!!)
        setContent {
            var name by remember { mutableStateOf(if (file.isDirectory) "Untitled.sh" else file.name) }
            var content by remember { mutableStateOf(if (file.isDirectory) "echo Hi" else file.readText()) }
            LazyList(anchorType = ScalingLazyListAnchorType.ItemStart) {
                item {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            color = primary_color,
                            fontFamily = font1
                        )
                    )
                }
                item {
                    BasicTextField(modifier = Modifier.defaultMinSize(minHeight = 200.dp),
                        value = content,
                        onValueChange = { content = it },
                        textStyle = TextStyle(fontFamily = font1, color = primary_color),
                        decorationBox = {
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .background(
                                        surfaceColor, RoundedCornerShape(25.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                it()
                                Button(modifier = Modifier
                                    .fillMaxWidth()
                                    .background(primary_color, RoundedCornerShape(25))
                                    .padding(10.dp)
                                    .align(Alignment.End),
                                    color = surfaceColor,
                                    icon = Icons.TwoTone.Save,
                                    text = "Save"
                                ) {
                                    if (file.isDirectory) {
                                        File(file, name).also {
                                            it.createNewFile()
                                            it.setWritable(true)
                                            it.setReadable(true)
                                            it.setExecutable(true)
                                            it.writeText(content)
                                        }
                                    } else {
                                        file.writeText(content)
                                        if (name != file.name) {
                                            file.renameTo(File(file.parentFile, name))
                                        }
                                    }

                                    finish()
                                }
                            }
                        })
                }
            }
        }
    }
}