package com.nyxhub.presentation

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.system.Os
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import com.nyxhub.nyx.NyxConstants
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.LazyList
import com.termux.nyxhub.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.File

val font1 = FontFamily(Font(R.font.open_sans))

fun startActivity(context: Context, cls: Class<*>) {
    context.startActivity(Intent(context, cls))
}

fun startNyx(context: Context, cmd: String? = null) {
    context.startActivity(Intent().apply {
        setComponent(
            ComponentName("com.termux", "com.termux.NyxActivity")
        )
        if (!cmd.isNullOrBlank()) putExtra("cmd", cmd)
    })
}

@Language("AGSL")
private const val SHADER2 = """
    uniform float2 size;
    uniform float time;
float f(float2 p)
{
    return sin(p.x+sin(p.y+time*0.1)) * sin(p.y*p.x*0.1+time*0.2);
}


//---------------Field to visualize defined here-----------------
float2 field(float2 p)
{
	float2 ep = float2(.05,0.);
    float2 rz= float2(0);
	for( int i=0; i<7; i++ )
	{
		float t0 = f(p);
		float t1 = f(p + ep.xy);
		float t2 = f(p + ep.yx);
        float2 g = float2((t1-t0), (t2-t0))/ep.xx;
		float2 t = float2(-g.y,g.x);
        
        p += .9*t + g*0.3;
        rz= t;
	}
    
    return rz;
}
//---------------------------------------------------------------


half4 main( float2 fragCoord )
{
	float2 p = fragCoord / size;
    p *= 6.;
	
    float2 fld = field(p);
    float col = sin(fld.x-fld.y)*0.7;
    
   	return half4(col);
}
"""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        CoroutineScope(Dispatchers.IO).launch {
            validateDir(NyxConstants.TERMUX_FILES_DIR_PATH)
            validateDir(NyxConstants.TERMUX_APPS_DIR_PATH)
            validateDir(NyxConstants.TERMUX_HOME_DIR_PATH)
            setupStorageSymlinks(this@MainActivity)
            if (!File(NyxConstants.CONFIG_PATH).exists()) File(NyxConstants.CONFIG_PATH).mkdirs()
            cacheDir.deleteRecursively()
        }
        setContent {
            Page_1()
        }
    }

    private val shape = RoundedCornerShape(25)

    @Composable
    fun Page_1() {
        var time by remember { mutableFloatStateOf(0f) }

        LazyList {
            item {
                Text(
                    text = TimeTextDefaults.timeSource("hh:mm").currentTime, fontFamily = font1
                )
            }
            item {
                Button(icon = Icons.TwoTone.SettingsSuggest,
                    text = "Setup",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(shape)
                        .drawWithCache {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val shader = RuntimeShader(SHADER2)
                                val shaderBrush = ShaderBrush(shader)
                                shader.setFloatUniform("size", size.width, size.height)

                                onDrawBehind {
                                    shader.setFloatUniform("time", time)
                                    drawRect(shaderBrush)
                                }
                            } else {
                                onDrawBehind {
                                    drawRect(surfaceColor)
                                }
                            }
                        }
                        .padding(horizontal = 35.dp)) {
                    startActivity(this@MainActivity, Presets::class.java)
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .clickable {
                                startActivity(this@MainActivity, DataChannel::class.java)
                            }
                            .fillMaxSize()
                            .clip(shape)
                            .weight(1f)
                            .background(primary_color)) {
                        Icon(
                            imageVector = Icons.TwoTone.SettingsRemote,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Text(text = "Receiver", fontFamily = font1, color = Color.Black)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Text(text = "Script",
                            modifier = Modifier
                                .clickable {
                                    startActivity(
                                        this@MainActivity, Scripts::class.java
                                    )
                                }
                                .fillMaxSize()
                                .weight(1f)
                                .background(surfaceColor, RoundedCornerShape(50))
                                .wrapContentHeight(),
                            fontFamily = font1,
                            textAlign = TextAlign.Center)
                        Icon(imageVector = Icons.Rounded.PlayArrow,
                            null,
                            modifier = Modifier
                                .clickable { startNyx(this@MainActivity) }
                                .fillMaxSize()
                                .border(1.dp, primary_color, RoundedCornerShape(50))
                                .weight(1f))
                    }
                }
            }
            item {
                Icon(imageVector = Icons.TwoTone.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            startActivity(
                                this@MainActivity, DetailedActivity::class.java
                            )
                        }
                        .fillMaxWidth())
            }
        }

        LaunchedEffect(key1 = time) {
            time += 0.01f
        }
    }

}

fun setupStorageSymlinks(context: Context) {
    try {
        val storageDir = NyxConstants.TERMUX_STORAGE_HOME_DIR
        val error: Boolean = storageDir.deleteRecursively() && storageDir.mkdirs()
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
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        Os.symlink(
            musicDir.absolutePath, File(storageDir, "music").absolutePath
        )
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
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
        var dirs = context.getExternalFilesDirs(null)
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
        dirs = context.externalMediaDirs
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
    }

}