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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.nyxhub.file.FileChooser
import com.nyxhub.file.key
import com.termux.shared.termux.NyxConstants.CONFIG_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

const val demo =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ\nabcdefghijklmnopqrstuvwxyz\n0123456789\n~!@#$%^&*()_+`-={}[]|\\:\";'<>?,./'"

class FontChooser : ComponentActivity() {
    val file=File("$CONFIG_PATH/font.ttf")
    private var currentFont: Typeface by mutableStateOf(Typeface.MONOSPACE)
    private val customFonts = mutableStateListOf<Typeface>()
    private val customFontsPath= mutableSetOf<String>()
    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private val result = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data=it.data?.getStringExtra(key)
                    val font=Typeface.createFromFile(data)
                    customFonts.add(font)
                    customFontsPath.add(data!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun saveCustomFonts(){
        getSharedPreferences("settings", MODE_PRIVATE).edit().putStringSet("customFonts", customFontsPath).apply()
    }
    private fun loadFonts(){
        getSharedPreferences("settings", MODE_PRIVATE).getStringSet("customFonts", setOf())?.forEach {
            try {
                customFonts.add(Typeface.createFromFile(it))
                customFontsPath.add(it)
            } catch (e: Exception) { }
        }
        currentFont= try{ Typeface.createFromFile("$CONFIG_PATH/font.ttf") }catch (e:Exception){Typeface.MONOSPACE}
    }

    override fun onPause() {
        super.onPause()
        saveCustomFonts()
    }

    override fun onResume() {
        super.onResume()
        loadFonts()
    }
    @OptIn(ExperimentalHorologistApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            val state = rememberScalingLazyListState()
            ScalingLazyColumn(state = state, modifier = Modifier.rotaryWithScroll(state)) {
                item {
                    Text(text = "Font Chooser", fontFamily = font1, color = Color.White)
                }
                item {
                    var show by remember{ mutableStateOf(false) }
                    val fontFamily=remember{ FontFamily(currentFont) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0B0B61), // Midnight Blue
                                        Color(0xFF191970), // Prussian Blue
                                        Color(0xFF3F397D), // Raisin Black
                                        Color(0xFF4C3F91)  // Cosmic Latte
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                ), RoundedCornerShape(25.dp)
                            )
                            .clickable { show = !show }
                            .padding(15.dp).animateContentSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.weight(2f),
                                text = "Current",
                                fontFamily = fontFamily,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Icon(
                                imageVector = Icons.TwoTone.Delete,
                                contentDescription = null,
                                tint = surfaceColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        Color.White, CircleShape
                                    )
                                    .padding(5.dp)
                                    .clickable {
                                        file.delete();loadFonts()
                                    }
                            )
                        }
                        if (show) {
                            Text(
                                text = demo,
                                fontFamily = fontFamily,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Open Nyx",
                                fontFamily = fontFamily,
                                color =Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.background(Color.White, RoundedCornerShape(25.dp)).padding(15.dp).fillMaxWidth().clickable { startNyx(this@FontChooser) })
                        }
                    }
                }
                items(assets.list("fonts")!!.sorted()) {
                    var show by remember { mutableStateOf(false) }
                    val fontFamily = remember { FontFamily(Typeface.createFromAsset(assets,"fonts/$it")) }
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            surfaceColor, RoundedCornerShape(25.dp)
                        )
                        .padding(15.dp)
                        .clickable { show = !show }
                        .animateContentSize(),
                        verticalArrangement = Arrangement.SpaceEvenly) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.weight(2f),
                                text = it.replace(".ttf", ""),
                                fontFamily = fontFamily,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Icon(
                                imageVector = Icons.TwoTone.Download,
                                contentDescription = null,
                                tint = surfaceColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        Color.White, CircleShape
                                    )
                                    .padding(5.dp)
                                    .clickable {
                                        file.delete()
                                        assets
                                            .open(it)
                                            .copyTo(FileOutputStream(file));loadFonts()
                                    }
                            )
                        }
                        if (show) Text(
                            text = demo,
                            fontFamily = fontFamily,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                items(customFonts){
                    var show by remember { mutableStateOf(false) }
                    val fontFamily = remember { FontFamily(it) }
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            surfaceColor, RoundedCornerShape(25.dp)
                        )
                        .padding(15.dp)
                        .clickable { show = !show }
                        .animateContentSize(),
                        verticalArrangement = Arrangement.SpaceEvenly) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.weight(2f),
                                text = "Font ${customFonts.indexOf(it)}",
                                fontFamily = fontFamily,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Icon(
                                imageVector = Icons.TwoTone.Download,
                                contentDescription = null,
                                tint = surfaceColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        Color.White, CircleShape
                                    )
                                    .padding(5.dp)
                                    .clickable {
                                        val file = File("$CONFIG_PATH/font.ttf")
                                        file.delete()
                                        File(
                                            customFontsPath.elementAt(
                                                customFonts.indexOf(
                                                    it
                                                )
                                            )
                                        ).copyTo(file);loadFonts()
                                    }
                            )
                        }
                        if (show) Text(
                            text = demo,
                            fontFamily = fontFamily,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                item {
                    Text(
                        text = "+ Add",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xff242124), RoundedCornerShape(25.dp)
                            )
                            .padding(20.dp)
                            .clickable {
                                result.launch(
                                    Intent(
                                        this@FontChooser, FileChooser::class.java
                                    ).putStringArrayListExtra(
                                        "filters", arrayListOf("ttf")
                                    )
                                )
                            },
                        fontFamily = font1,
                        color = Color(0xffFEF9EF),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

