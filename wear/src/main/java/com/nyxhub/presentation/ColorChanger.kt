package com.nyxhub.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Help
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Done
import androidx.compose.material.icons.twotone.FormatColorText
import androidx.compose.material.icons.twotone.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.nyxhub.nyx.Properties
import com.nyxhub.presentation.ui.ButtonTransparent
import com.termux.shared.termux.NyxConstants.CONFIG_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

val surfaceColor = Color(0xff18181c)

class ColorChanger : ComponentActivity() {
    private var blur by mutableStateOf(false)
    private val properties=Properties("$CONFIG_PATH/colors")
    private val DEFAULT_COLORSCHEME = listOf(
        // 16 original colors. First 8 are dim. // black
        -0x1000000,  // dim red
        -0x330000,  // dim green
        -0xff3300,  // dim yellow
        -0x323300,  // dim blue
        -0x9b6a13,  // dim magenta
        -0x32ff33,  // dim cyan
        -0xff3233,  // dim white
        -0x1a1a1b,  // Second 8 are bright: // medium grey
        -0x808081,  // bright red
        -0x10000,  // bright green
        -0xff0100,  // bright yellow
        -0x100,  // light blue
        -0xa3a301,  // bright magenta
        -0xff01,  // bright cyan
        -0xff0001,  // bright white
        -0x1,  // 216 color cube, six shades of each color:
        -0x1000000,
        -0xffffa1,
        -0xffff79,
        -0xffff51,
        -0xffff29,
        -0xffff01,
        -0xffa100,
        -0xffa0a1,
        -0xffa079,
        -0xffa051,
        -0xffa029,
        -0xffa001,
        -0xff7900,
        -0xff78a1,
        -0xff7879,
        -0xff7851,
        -0xff7829,
        -0xff7801,
        -0xff5100,
        -0xff50a1,
        -0xff5079,
        -0xff5051,
        -0xff5029,
        -0xff5001,
        -0xff2900,
        -0xff28a1,
        -0xff2879,
        -0xff2851,
        -0xff2829,
        -0xff2801,
        -0xff0100,
        -0xff00a1,
        -0xff0079,
        -0xff0051,
        -0xff0029,
        -0xff0001,
        -0xa10000,
        -0xa0ffa1,
        -0xa0ff79,
        -0xa0ff51,
        -0xa0ff29,
        -0xa0ff01,
        -0xa0a100,
        -0xa0a0a1,
        -0xa0a079,
        -0xa0a051,
        -0xa0a029,
        -0xa0a001,
        -0xa07900,
        -0xa078a1,
        -0xa07879,
        -0xa07851,
        -0xa07829,
        -0xa07801,
        -0xa05100,
        -0xa050a1,
        -0xa05079,
        -0xa05051,
        -0xa05029,
        -0xa05001,
        -0xa02900,
        -0xa028a1,
        -0xa02879,
        -0xa02851,
        -0xa02829,
        -0xa02801,
        -0xa00100,
        -0xa000a1,
        -0xa00079,
        -0xa00051,
        -0xa00029,
        -0xa00001,
        -0x790000,
        -0x78ffa1,
        -0x78ff79,
        -0x78ff51,
        -0x78ff29,
        -0x78ff01,
        -0x78a100,
        -0x78a0a1,
        -0x78a079,
        -0x78a051,
        -0x78a029,
        -0x78a001,
        -0x787900,
        -0x7878a1,
        -0x787879,
        -0x787851,
        -0x787829,
        -0x787801,
        -0x785100,
        -0x7850a1,
        -0x785079,
        -0x785051,
        -0x785029,
        -0x785001,
        -0x782900,
        -0x7828a1,
        -0x782879,
        -0x782851,
        -0x782829,
        -0x782801,
        -0x780100,
        -0x7800a1,
        -0x780079,
        -0x780051,
        -0x780029,
        -0x780001,
        -0x510000,
        -0x50ffa1,
        -0x50ff79,
        -0x50ff51,
        -0x50ff29,
        -0x50ff01,
        -0x50a100,
        -0x50a0a1,
        -0x50a079,
        -0x50a051,
        -0x50a029,
        -0x50a001,
        -0x507900,
        -0x5078a1,
        -0x507879,
        -0x507851,
        -0x507829,
        -0x507801,
        -0x505100,
        -0x5050a1,
        -0x505079,
        -0x505051,
        -0x505029,
        -0x505001,
        -0x502900,
        -0x5028a1,
        -0x502879,
        -0x502851,
        -0x502829,
        -0x502801,
        -0x500100,
        -0x5000a1,
        -0x500079,
        -0x500051,
        -0x500029,
        -0x500001,
        -0x290000,
        -0x28ffa1,
        -0x28ff79,
        -0x28ff51,
        -0x28ff29,
        -0x28ff01,
        -0x28a100,
        -0x28a0a1,
        -0x28a079,
        -0x28a051,
        -0x28a029,
        -0x28a001,
        -0x287900,
        -0x2878a1,
        -0x287879,
        -0x287851,
        -0x287829,
        -0x287801,
        -0x285100,
        -0x2850a1,
        -0x285079,
        -0x285051,
        -0x285029,
        -0x285001,
        -0x282900,
        -0x2828a1,
        -0x282879,
        -0x282851,
        -0x282829,
        -0x282801,
        -0x280100,
        -0x2800a1,
        -0x280079,
        -0x280051,
        -0x280029,
        -0x280001,
        -0x10000,
        -0xffa1,
        -0xff79,
        -0xff51,
        -0xff29,
        -0xff01,
        -0xa100,
        -0xa0a1,
        -0xa079,
        -0xa051,
        -0xa029,
        -0xa001,
        -0x7900,
        -0x78a1,
        -0x7879,
        -0x7851,
        -0x7829,
        -0x7801,
        -0x5100,
        -0x50a1,
        -0x5079,
        -0x5051,
        -0x5029,
        -0x5001,
        -0x2900,
        -0x28a1,
        -0x2879,
        -0x2851,
        -0x2829,
        -0x2801,
        -0x100,
        -0xa1,
        -0x79,
        -0x51,
        -0x29,
        -0x1,  // 24 grey scale ramp:
        -0xf7f7f8,
        -0xededee,
        -0xe3e3e4,
        -0xd9d9da,
        -0xcfcfd0,
        -0xc5c5c6,
        -0xbbbbbc,
        -0xb1b1b2,
        -0xa7a7a8,
        -0x9d9d9e,
        -0x939394,
        -0x89898a,
        -0x7f7f80,
        -0x757576,
        -0x6b6b6c,
        -0x616162,
        -0x575758,
        -0x4d4d4e,
        -0x434344,
        -0x39393a,
        -0x2f2f30,
        -0x252526,
        -0x1b1b1c,
        -0x111112,
        -0x1,// COLOR_INDEX_DEFAULT_FOREGROUND, and :
        -0x1000000,//COLOR_INDEX_DEFAULT_BACKGROUND
        -0x1,//COLOR_INDEX_DEFAULT_CURSOR
        -0x1,//COLOR_PRIMARY_UI
        -0x1000000//Color_SECENDARY_UI
    )

    @OptIn(ExperimentalStdlibApi::class, ExperimentalHorologistApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch { loadColors() }
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            val gridState = rememberScalingLazyListState()
            var index by remember { mutableIntStateOf(0) }
            var text by remember { mutableStateOf("#000000") }
            Scaffold(positionIndicator = { PositionIndicator(scalingLazyListState = gridState) }) {
                ScalingLazyColumn(
                    state = gridState,
                    modifier = Modifier
                        .background(Color(0xff0B0B0B))
                        .blur(if (blur) 20.dp else 0.dp)
                        .rotaryWithScroll(gridState),
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.animateContentSize()
                        ) {
                            val annotatedString = remember {
                                buildAnnotatedString {
                                    append("First 256 colors are indexed according to ")

                                    pushStringAnnotation(
                                        tag = "info",
                                        annotation = "https://www.ditig.com/publications/256-colors-cheat-sheet"
                                    )
                                    withStyle(style = SpanStyle(color = Color(0xff8692FC))) {
                                        append("xterm-256")
                                    }
                                    append("\n Note :\nColor 256 : Foreground Color\nColor257 : Background color\nColor258 : Cursor color\nColor259 : Primary Ui color\nColor260: Secondary Ui Color")
                                    pop()
                                }
                            }
                            var help by remember {
                                mutableStateOf(false)
                            }

                            if (help) {
                                Text(
                                    text = annotatedString,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    fontFamily = font1
                                )
                            } else {
                                Text(
                                    text = "Colors",
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    fontFamily = font1
                                )
                            }
                            Icon(imageVector = Icons.AutoMirrored.TwoTone.Help,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable { help = !help }
                                    .padding(5.dp),
                                tint = Color.White)

                        }
                    }
                    items(colors.size) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(surfaceColor)
                                .height(60.dp)
                                .fillMaxWidth()
                                .clickable {
                                    index = it;text = "#${colors[it].toHexString()}"; blur = true
                                }) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(color = Color(colors[it]))
                            )
                            Text(
                                text = "Color $it",
                                color = Color(0xffFEF9EF),
                                fontFamily = font1,
                                modifier = Modifier.padding(10.dp)
                            )
                            if (DEFAULT_COLORSCHEME[it] != colors[it]) Icon(imageVector = Icons.TwoTone.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable {
                                        colors[it] = DEFAULT_COLORSCHEME[it]
                                        properties.remove(it)
                                    }
                                    .padding(10.dp),
                                tint = Color.White)
                        }

                    }


                }
                if (blur) Column(verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)) {
                    val newColor = try {
                        Color(text.toColorInt())
                    } catch (e: Exception) {
                        Color.White
                    }
                    BasicTextField(
                        decorationBox = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .border(1.dp, newColor, RoundedCornerShape(25))
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.TwoTone.FormatColorText,
                                    contentDescription = null,
                                    tint = newColor
                                )
                                it()
                            }
                        },
                        value = text,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        textStyle = TextStyle(
                            color = newColor, fontFamily = font1
                        ),
                        onValueChange = { text = it },
                        cursorBrush = SolidColor(newColor)
                    )
                    Button(icon = Icons.TwoTone.Done, text = "Apply") {
                        colors[index] = newColor.toArgb()
                        properties.put(index,colors[index])
                    }
                    Button(icon = Icons.TwoTone.Cancel, text = "Cancel")
                }

            }

        }
    }

    @Composable
    fun Button(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
        ButtonTransparent(icon = icon, text = text, onClick = { onClick();blur = false })
    }

    override fun onPause() {
        super.onPause()
        CoroutineScope(Dispatchers.IO).launch { properties.save() }
    }

    private val colors = mutableStateListOf<Int>().apply { addAll(DEFAULT_COLORSCHEME) }


    private fun loadColors() {
        properties.forEach { key, value ->
            colors[key.toInt()] = value.toInt()
        }
    }
}


