package com.nyxhub.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Description
import androidx.compose.material.icons.twotone.EditNote
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.SwipeToReveal
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter

class Scripts : ComponentActivity() {
    private val files = mutableStateListOf<File>()

    @OptIn(ExperimentalWearFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        files.apply { filesDir.listFiles(FileFilter { it.extension == "sh" })?.let { addAll(it) } }
        val scope = CoroutineScope(Dispatchers.Main)
        setContent {

            LazyList {
                item {
                    Text(text = "Scripts", color = primary_color, fontFamily = font1)
                }
                items(files) {
                    val state = rememberRevealState()
                    SwipeToReveal(state = state, primaryAction = {
                        Icon(imageVector = Icons.TwoTone.Delete,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                files.remove(it)
                                it.delete()
                            })

                    }, onFullSwipe = {
                        files.remove(it)
                        it.delete()
                        scope.launch { state.snapTo(RevealValue.Covered) }
                    }, secondaryAction = {
                        Icon(imageVector = Icons.TwoTone.EditNote,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                EditFile(
                                    this@Scripts, it
                                )
                                scope.launch { state.snapTo(RevealValue.Covered) }
                            })
                    }) {
                        Button(icon = Icons.TwoTone.Description, text = it.name) {
                            startNyx(this@Scripts,"bash ${it.absolutePath}\n")
                        }
                    }

                }
                item {
                    Icon(imageVector = Icons.TwoTone.Add,
                        contentDescription = null,
                        tint = surfaceColor,
                        modifier = Modifier
                            .clickable {
                                EditFile(this@Scripts, filesDir)
                            }
                            .padding(5.dp)
                            .background(
                                primary_color, RoundedCornerShape(50)
                            )
                            .padding(5.dp)
                            .size(50.dp, 25.dp))
                }
            }


        }
    }

    override fun onResume() {
        super.onResume()
        files.clear()
        files.apply { filesDir.listFiles(FileFilter { it.extension == "sh" })?.let { addAll(it) } }
    }
}

fun EditFile(context: Context, file: File) {
    context.startActivity(
        Intent(context, TextEditor::class.java).putExtra(
            "file", file.absolutePath
        )
    )
}