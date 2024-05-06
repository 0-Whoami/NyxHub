package com.nyxhub.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.system.Os
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.twotone.ExpandMore
import androidx.compose.material.icons.twotone.SettingsRemote
import androidx.compose.material.icons.twotone.SettingsSuggest
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import com.nyxhub.file.FileUtils
import com.termux.nyxhub.R
import com.termux.shared.termux.NyxConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

val font1 = FontFamily(Font(R.font.open_sans))
fun startActivity(context: Context,cls:Class<*>){
    context.startActivity(Intent(context,cls))
}
fun startNyx(context: Context) {
    context.startActivity(Intent().apply {
        setComponent(
            ComponentName("com.termux", "com.termux.app.main")
        )
    })
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        CoroutineScope(Dispatchers.IO).launch {
            FileUtils.isTermuxFilesDirectoryAccessible(
                createDirectoryIfMissing = true, setMissingPermissions = true
            )
            FileUtils.isAppsTermuxAppDirectoryAccessible(
                true, setMissingPermissions = true
            )
            setupStorageSymlinks()
            if (!File(NyxConstants.CONFIG_PATH).exists()) File(NyxConstants.CONFIG_PATH).mkdirs()
        }
        setContent {
            Page_1()
        }
    }

    private val shape = RoundedCornerShape(25)

    @Composable
    fun Page_1() {
        ScalingLazyColumn {
            item {
                Text(
                    text = TimeTextDefaults.timeSource("hh:mm").currentTime,
                    fontFamily = font1
                )
            }
            item {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(25))
                        .height(75.dp)
                        .fillMaxWidth()
                        .clickable { startActivity(this@MainActivity,Presets::class.java) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(10.dp),
                        painter = painterResource(id = R.drawable.sky),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Icon(imageVector = Icons.TwoTone.SettingsSuggest, contentDescription = null)
                        Text(text = "Setup", fontFamily = font1)


                    }
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(primary_color, shape)
                            .weight(1f)
                            .clickable {
                                startActivity(this@MainActivity,DataChannel::class.java)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.SettingsRemote,
                            contentDescription = null,
                            tint = surfaceColor
                        )
                        Text(text = "Receiver", fontFamily = font1, color = surfaceColor)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Text(
                            text = "Script",
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxSize()
                                .weight(1f)
                                .background(surfaceColor, RoundedCornerShape(50))
                                .wrapContentHeight()
                                .clickable { startActivity(this@MainActivity,Scripts::class.java) },
                            fontFamily = font1,
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            null,
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxSize()
                                .border(1.dp, primary_color, RoundedCornerShape(50))
                                .weight(1f)
                                .clickable { startNyx(this@MainActivity) }
                        )
                    }
                }
            }
            item {
                Icon(
                    imageVector = Icons.TwoTone.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().clickable { startActivity(this@MainActivity,DetailedActivity::class.java) }
                )
            }
        }
    }

    private fun setupStorageSymlinks() {
        try {
            val error: Boolean
            val storageDir = NyxConstants.TERMUX_STORAGE_HOME_DIR
            error = FileUtils.clearDirectory(
                storageDir.absolutePath
            )
            if (!error) {
                return
            }
            // Get primary storage root "/storage/emulated/0" symlink
            val sharedDir = Environment.getExternalStorageDirectory()
            Os.symlink(
                sharedDir.absolutePath, File(storageDir, "utils").absolutePath
            )
            val documentsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            Os.symlink(
                documentsDir.absolutePath, File(storageDir, "documents").absolutePath
            )
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            Os.symlink(
                downloadsDir.absolutePath, File(storageDir, "downloads").absolutePath
            )
            val dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            Os.symlink(
                dcimDir.absolutePath, File(storageDir, "dcim").absolutePath
            )
            val picturesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            Os.symlink(
                picturesDir.absolutePath, File(storageDir, "pictures").absolutePath
            )
            val musicDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            Os.symlink(
                musicDir.absolutePath, File(storageDir, "music").absolutePath
            )
            val moviesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            Os.symlink(
                moviesDir.absolutePath, File(storageDir, "movies").absolutePath
            )
            val podcastsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
            Os.symlink(
                podcastsDir.absolutePath, File(storageDir, "podcasts").absolutePath
            )
            val audiobooksDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_AUDIOBOOKS)
            Os.symlink(
                audiobooksDir.absolutePath, File(storageDir, "audiobooks").absolutePath
            )

            // Create "Android/data/com.termux" symlinks
            var dirs = getExternalFilesDirs(null)
            if (dirs != null && dirs.isNotEmpty()) {
                for (i in dirs.indices) {
                    val dir = dirs[i] ?: continue
                    val symlinkName = "external-$i"
                    Os.symlink(
                        dir.absolutePath, File(storageDir, symlinkName).absolutePath
                    )
                }
            }
            // Create "Android/media/com.termux" symlinks
            dirs = externalMediaDirs
            if (dirs != null && dirs.isNotEmpty()) {
                for (i in dirs.indices) {
                    val dir = dirs[i] ?: continue
                    val symlinkName = "media-$i"
                    Os.symlink(
                        dir.absolutePath, File(storageDir, symlinkName).absolutePath
                    )
                }
            }
        } catch (error: Exception) {
            error.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
            }
        }

    }

}
