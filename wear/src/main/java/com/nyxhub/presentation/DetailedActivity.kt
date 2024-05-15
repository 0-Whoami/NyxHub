package com.nyxhub.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Message
import androidx.compose.material.icons.twotone.ColorLens
import androidx.compose.material.icons.twotone.FontDownload
import androidx.compose.material.icons.twotone.InsertLink
import androidx.compose.material.icons.twotone.KeyboardCommandKey
import androidx.compose.material.icons.twotone.Wallpaper
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.wear.compose.material.Text
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import com.termux.nyxhub.R

class DetailedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContent {
            LazyList {
                item {
                    Text(text = "Settings", fontFamily = font1, color = primary_color)
                }
                item {
                    Button(icon = Icons.TwoTone.ColorLens, text = "Colors") {
                        startActivity(this@DetailedActivity, ColorChanger::class.java)
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.Wallpaper, text = "Window") {
                        startActivity(this@DetailedActivity, BackgroundManager::class.java)
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.FontDownload, text = "Font") {
                        startActivity(this@DetailedActivity, FontChooser::class.java)
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.KeyboardCommandKey, text = "Keys") {
                        startActivity(this@DetailedActivity, KeyEditor::class.java)
                    }
                }
                item {
                    Button(
                        icon = ImageVector.vectorResource(id = R.drawable.bash),
                        text = "Shell"
                    ) {}
                }
                item {
                    Button(icon = Icons.AutoMirrored.TwoTone.Message, text = "Monet") {
                        startActivity(this@DetailedActivity, ColorChanger::class.java)
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.InsertLink , text = "Fix Symlink") {

                    }
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

}