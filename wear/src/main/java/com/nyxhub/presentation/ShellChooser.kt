package com.nyxhub.presentation

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.DownloadDone
import androidx.compose.material.icons.twotone.Error
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.nyxhub.nyx.NyxConstants
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import java.io.File

class ShellChooser : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        val listOfShells =
            listOf("sh", "bash", "zsh", "fish").associateWith { false }.toMutableMap()
        val file = File(NyxConstants.TERMUX_BIN_PREFIX_DIR_PATH)
        val existence = file.exists()
        if (existence) {
            file.list()?.forEach {
                if (listOfShells.contains(it)) listOfShells[it] = true
            }
        }
        setContent {
            if (!existence) Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.TwoTone.Error, null,tint = MaterialTheme.colors.error)
                Text("BootStrap is not installed", fontFamily = font1)
            }
            else LazyList {
                item { Text("Shells", fontFamily = font1) }
                items(listOfShells.size) {
                    val (key, value) = listOfShells.entries.elementAt(it)
                    Button(
                        icon = if (value) Icons.TwoTone.DownloadDone else Icons.TwoTone.Download,
                        text = key) {
                        val stringBuilder = StringBuilder()
                        if (!value) stringBuilder.append("pkg install $key -y && ")
                        stringBuilder.append("chsh -s $key && exit\n")
                        startNyx(this@ShellChooser, stringBuilder.toString())
                    }
                }
            }
        }
    }

}