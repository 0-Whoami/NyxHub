package com.nyxhub.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Message
import androidx.compose.material.icons.twotone.AutoFixHigh
import androidx.compose.material.icons.twotone.ColorLens
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.FontDownload
import androidx.compose.material.icons.twotone.InsertLink
import androidx.compose.material.icons.twotone.KeyboardCommandKey
import androidx.compose.material.icons.twotone.ViewInAr
import androidx.compose.material.icons.twotone.Wallpaper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.wear.compose.material.Text
import com.nyxhub.nyx.NyxConstants
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.presentation.ui.Loading
import com.termux.nyxhub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class DetailedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var loading by mutableStateOf(false)
        val scope = CoroutineScope(Dispatchers.IO)
        setContent {
            LazyList(blur = loading) {
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
                        icon = ImageVector.vectorResource(id = R.drawable.bash), text = "Shell"
                    ) { startActivity(this@DetailedActivity, ShellChooser::class.java) }
                }
                item {
                    Button(icon = Icons.AutoMirrored.TwoTone.Message, text = "MOTD") {
                        EditFile(this@DetailedActivity,
                            File("${NyxConstants.TERMUX_PREFIX_DIR_PATH}/etc/motd")
                        )
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.InsertLink, text = "Fix Symlink") {
                        scope.launch {
                            loading = true;setupStorageSymlinks(this@DetailedActivity);loading =
                            false
                        }
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.Download, text = "Install Bootstrap") {
                        scope.launch {
                            startActivity(
                                this@DetailedActivity,
                                BootStrapChooser::class.java
                            )
                        }
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.AutoFixHigh, text = "Apply Patch") {
                        scope.launch {
                            loading = true;applyPatch(this@DetailedActivity);loading = false
                        }
                    }
                }
                item {
                    Button(icon = Icons.TwoTone.ViewInAr, text = "Fix Variables") {
                        scope.launch {
                            loading = true;environmentVariable();loading = false
                        }
                    }
                }
            }
            if (loading) Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { Loading() }
        }
        super.onCreate(savedInstanceState)
    }

}