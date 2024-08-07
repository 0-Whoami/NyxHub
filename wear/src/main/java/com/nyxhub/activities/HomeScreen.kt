package com.nyxhub.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.itemsIndexed
import com.nyxhub.base.Card
import com.nyxhub.base.LazyListWrapper
import com.nyxhub.base.Text
import com.nyxhub.base.VerticalDivider
import com.nyxhub.base.cornerBorder
import com.nyxhub.base.time
import com.nyxhub.presentation.primary_color

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        val list = listOf("PRESETS", "UI", "EXTRA KEYS", "TERM COLORS")
        val listOfAction = listOf({},
                                  { startActivity(Intent(this, UIEditor::class.java)) },
                                  { BatchPropertyEditor.editKeys(this) },
                                  { BatchPropertyEditor.editColors(this) })
        setContent {
            LazyListWrapper(0) { ci ->
                item {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = time("hh") + "\n" + time("mm"),
                             modifier = Modifier
                                 .size(90.dp)
                                 .cornerBorder()
                                 .wrapContentHeight(),
                             fontSize = 30.sp,
                             color = primary_color,
                             fontWeight = FontWeight.Bold)
                        Column(modifier = Modifier
                            .weight(1f)
                            .height(90.dp)) {
                            Text("HELLO",
                                 modifier = Modifier
                                     .height(45.dp)
                                     .fillParentMaxWidth()
                                     .padding(start = 5.dp, bottom = 5.dp)
                                     .border(1.dp, primary_color)
                                     .wrapContentHeight())
                            Canvas(Modifier
                                       .fillParentMaxWidth()
                                       .height(45.dp)) {
                                clipRect {
                                    inset(5.dp.toPx() + 2, 4f, 4f, 4f) {
                                        val w = size.width / 10
                                        val h = size.height / 5
                                        for (i in 0..10) {
                                            for (j in 0..5) {
                                                drawCircle(primary_color, 4f, center = Offset(i * w, j * h))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                itemsIndexed(list) { i, it ->
                    VerticalDivider()
                    Card(text = it, focused = i == ci + 1, click = listOfAction[i])
                }
            }
        }
    }
}
