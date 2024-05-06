package com.nyxhub.presentation

import android.os.Build
import android.os.Bundle
import android.system.Os
import android.util.Pair
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ChevronRight
import androidx.compose.material.icons.twotone.Link
import androidx.compose.material.icons.twotone.PhoneAndroid
import androidx.compose.material.icons.twotone.Watch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.nyxhub.file.FileUtils
import com.nyxhub.presentation.ui.Cell
import com.nyxhub.presentation.ui.Loading
import com.termux.shared.termux.NyxConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class Presets : ComponentActivity() {
    private var label by mutableStateOf("")
    private var loading by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {print()}
        setContent {
            var url by remember { mutableStateOf(determineZipUrl()) }
            ScalingLazyColumn(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(if (loading) 10.dp else 0.dp)
            ) {
                item{ Text(text = "Bootstrap Installer", fontFamily = font1) }
                item {
                    Row {
                        Cell(icon = Icons.TwoTone.PhoneAndroid, text = "Default", modifier = Modifier.weight(1f)){ setupBootstrap(determineZipUrl()) }
                        Cell(icon = Icons.TwoTone.Watch, text = "Nyx", modifier = Modifier.weight(1f)){ setupBootstrap(determineZipUrl()) }
                    }
                }
                item { Text(text = "Custom Bootstrap", fontFamily = font1) }
                item{
                    OutlinedTextField(
                        label = { Text(text = "URL", fontFamily = font1)},
                        value = url,
                        onValueChange = { url = it },
                        textStyle = TextStyle(color = Color.White, fontFamily = font1),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Link, contentDescription = null
                            )
                        },
                        trailingIcon = { Icon(
                            imageVector = Icons.TwoTone.ChevronRight,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.clickable { setupBootstrap(url) }.background(Color.White,
                                CircleShape).padding(5.dp)
                        )},
                        modifier = Modifier.padding(5.dp),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White)
                    )
                }
            }
            if (loading) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize().clickable {  }
                ) {
                    Loading()
                    Text(text = label, fontFamily = font1)
                }
            }
        }
    }

    //TODO UPDATE {*Compile first}
    private fun determineZipUrl(): String {
        return "https://github.com/termux/termux-packages/releases/latest/download/bootstrap-" + determineTermuxArchName() + ".zip"
    }

    private fun determineTermuxArchName(): String {
        for (androidArch in Build.SUPPORTED_ABIS) {
            when (androidArch) {
                "armeabi-v7a" -> return "arm"
                "x86_64" -> return "x86_64"
            }
        }
        return ""
    }

    private fun ensureDirectoryExists(directory: File): Boolean {
        return FileUtils.createDirectoryFile(directory.absolutePath)
    }
    private fun String.deleteRecursively():Boolean{return File(this).deleteRecursively()}
    private fun setupBootstrap(url: String) {
        loading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var error: Boolean
                label = "Cleaning up..."
                error = NyxConstants.TERMUX_FILES_DIR_PATH.deleteRecursively()
                if (!error) {
                    label = "Can't delete ${NyxConstants.TERMUX_FILES_DIR_PATH}"
                    return@launch
                }
                // Delete prefix staging directory or any file at its destination
                error = NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH.deleteRecursively()
                if (!error) {
                    label = "Can't delete staging ${NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH}"
                    return@launch
                }
                // Delete prefix directory or any file at its destination
                error = NyxConstants.TERMUX_PREFIX_DIR_PATH.deleteRecursively()

                if (!error) {
                    label = "Can't delete ${NyxConstants.TERMUX_PREFIX_DIR_PATH}"
                    return@launch
                }
                // Create prefix staging directory if it does not already exist and set required permissions
                error = FileUtils.isTermuxPrefixStagingDirectoryAccessible(
                    createDirectoryIfMissing = true, setMissingPermissions = true
                )
                if (!error) {
                    label = "Can't create ${NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH}"
                    return@launch
                }
                // Create prefix directory if it does not already exist and set required permissions
                error = FileUtils.isTermuxPrefixDirectoryAccessible(
                    createDirectoryIfMissing = true, setMissingPermissions = true
                )
                if (!error) {
                    label = "Can't create ${NyxConstants.TERMUX_PREFIX_DIR_PATH}"
                    return@launch
                }
                val buffer = ByteArray(8096)
                val symlinks: MutableList<Pair<String, String>> = ArrayList(50)
                val zipUrl = URL(url)
                label = "Downloading...\nPlease be patient"
                ZipInputStream(zipUrl.openStream()).use { zipInput ->
                    var zipEntry: ZipEntry?
                    while (zipInput.nextEntry.also { zipEntry = it } != null) {
                        if (zipEntry!!.name == "SYMLINKS.txt") {
                            val symlinksReader = BufferedReader(InputStreamReader(zipInput))
                            var line: String?
                            while (symlinksReader.readLine().also { line = it } != null) {
                                val parts =
                                    line!!.split("←".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                if (parts.size != 2) {
                                    label = "Malformed symlink line: $line"
                                    return@launch
                                }
                                val oldPath = parts[0]
                                val newPath =
                                    NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH + "/" + parts[1]
                                symlinks.add(
                                    Pair.create(
                                        oldPath, newPath
                                    )
                                )
                                error = ensureDirectoryExists(
                                    File(newPath).parentFile!!
                                )
                                if (!error) {
                                    label = "Can't create symlink directory, $newPath"
                                    return@launch
                                }
                            }
                        } else {
                            val zipEntryName = zipEntry!!.name
                            val targetFile = File(
                                NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH, zipEntryName
                            )
                            val isDirectory = zipEntry!!.isDirectory
                            error =
                                ensureDirectoryExists(if (isDirectory) targetFile else targetFile.parentFile)
                            if (!error) {
                                label = "Can't create symlink directory, $targetFile"
                                return@launch
                            }
                            if (!isDirectory) {
                                FileOutputStream(targetFile).use { outStream ->
                                    var readBytes: Int
                                    while (zipInput.read(buffer).also { readBytes = it } != -1) {
                                        outStream.write(buffer, 0, readBytes)
                                    }
                                }
                                if (zipEntryName.startsWith("bin/") || zipEntryName.startsWith("libexec") || zipEntryName.startsWith(
                                        "lib/apt/apt-helper"
                                    ) || zipEntryName.startsWith("lib/apt/methods")
                                ) {
                                    Os.chmod(targetFile.absolutePath, 448)
                                }
                            }
                        }
                    }
                }
                if (symlinks.isEmpty()) {
                    label = "No Symlink text"
                    return@launch
                }
                for (symlink in symlinks) {
                    Os.symlink(symlink.first, symlink.second)
                }
                if (!NyxConstants.TERMUX_STAGING_PREFIX_DIR.renameTo(NyxConstants.TERMUX_PREFIX_DIR)) {
                    throw RuntimeException("Moving termux prefix staging to prefix directory failed")
                }
            } catch (exception: Exception) {
                label = exception.message ?: "Unknown error"
                exception.printStackTrace()
            }
            loading = false
            label = "Waiting for files..."
            runOnUiThread {
                Toast.makeText(this@Presets, "Done!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}