package com.nyxhub.presentation

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Bookmark
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material.icons.twotone.KeyboardCommandKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.nyxhub.nyx.Properties
import com.nyxhub.presentation.ui.AnimatedVisibility
import com.nyxhub.presentation.ui.ButtonTransparent
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.nyx.NyxConstants.CONFIG_PATH
const val KEYS_FILE_NAME="keys"
class KeyEditor : ComponentActivity() {
    private var blur by mutableStateOf(false)
    private val properties = Properties("$CONFIG_PATH/$KEYS_FILE_NAME")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKeys()
        setContent {

            var labelText by remember { mutableStateOf("") }
            var keycodeText by remember { mutableStateOf("") }

            LazyList(blur = blur) {
                item {
                    Text(
                        text = "Keys Editor",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontFamily = font1
                    )
                }
                items(changedMap.size) {
                    val label = changedMap.keys.elementAt(it)
                    val keycode = changedMap.values.elementAt(it)
                    Row(modifier = Modifier
                        .clickable {
                            labelText = label
                            keycodeText = keycode.toString()
                        }
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            Color(0xff242124), RoundedCornerShape(25)
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "${it + 1}. $label : $keycode",
                            modifier = Modifier.padding(start = 10.dp),
                            fontFamily = font1,
                            color = Color(0xffFEF9EF)
                        )
                        Icon(imageVector = Icons.TwoTone.Delete,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { changedMap.remove(label) }
                                .padding(5.dp))
                    }
                }
                item {
                    Icon(imageVector = Icons.TwoTone.Add,
                        contentDescription = null,
                        tint = surfaceColor,
                        modifier = Modifier
                            .clickable {
                                keycodeText = ""
                                labelText = ""
                                blur = true
                            }
                            .background(
                                primary_color, RoundedCornerShape(50)
                            )
                            .padding(10.dp)
                            .fillMaxWidth(if(changedMap.size==0) 1f else 0.5f))
                }
            }

            AnimatedVisibility(visible = blur) {
                LazyList {
                    item { Text(text = "Add Key", fontFamily = font1, color = primary_color) }
                    item {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            value = labelText,
                            textStyle = TextStyle(color = primary_color, fontFamily = font1),
                            onValueChange = { labelText = it },
                            decorationBox = {
                                if (labelText.isBlank()) Text(
                                    text = "↵",
                                    fontFamily = font1,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentSize()
                                        .alpha(0.5f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.TwoTone.Bookmark,
                                        contentDescription = null
                                    )
                                    it()
                                }
                            },
                            cursorBrush = SolidColor(primary_color)
                        )
                    }
                    item {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            value = keycodeText,
                            textStyle = TextStyle(fontFamily = font1, color = primary_color),
                            onValueChange = { keycodeText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decorationBox = {
                                if (keycodeText.isBlank()) Text(
                                    text = "${KeyEvent.KEYCODE_ENTER}",
                                    fontFamily = font1,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentSize()
                                        .alpha(0.5f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.TwoTone.KeyboardCommandKey,
                                        contentDescription = null
                                    )
                                    it()
                                }
                            },
                            cursorBrush = SolidColor(primary_color)
                        )
                    }

                    item {
                        Button_1(icon = Icons.TwoTone.Done, text = "Add") {
                            if (keycodeText.isEmpty()) return@Button_1
                            changedMap[labelText] = keycodeText.toInt()
                        }
                    }
                    item { Button_1(icon = Icons.TwoTone.Cancel, text = "Cancel") }
                }
            }


        }
    }

    @Composable
    fun Button_1(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
        ButtonTransparent(icon = icon, text = text, onClick = { onClick();blur = false })
    }

    override fun onPause() {
        super.onPause()
        saveKeys()
    }

    private val changedMap = mutableStateMapOf<String, Int>()

    private fun loadKeys() {
        changedMap.clear()
        properties.forEach { key, value ->
            changedMap[key] = value.toInt()
        }
    }

    private fun saveKeys() {
        properties.map.clear()
        changedMap.forEach { (key, value) ->
            properties.map[key] = value.toString()
        }
        properties.save()
    }


}
