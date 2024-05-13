package com.nyxhub.presentation

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.nyxhub.file.FileUtils
import com.nyxhub.presentation.ui.Button
import com.termux.nyxhub.R
import com.termux.shared.termux.NyxConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.File

val font1 = FontFamily(Font(R.font.open_sans))

fun startActivity(context: Context, cls: Class<*>) {
    context.startActivity(Intent(context, cls))
}

fun startNyx(context: Context) {
    context.startActivity(Intent().apply {
        setComponent(
            ComponentName("com.termux", "com.termux.app.main")
        )
    })
}

@Language("AGSL")
private const val SHADER3 = """
    uniform float2 size;
    uniform float time;
    
    
float rand(float2 n) { 
    return fract(sin(dot(n, float2(12.9898, 4.1414))) * 43758.5453);
}

float noise(float2 p){
    float2 ip = floor(p);
    float2 u = fract(p);
    u = u*u*(3.0-2.0*u);

    float res = mix(
        mix(rand(ip),rand(ip+float2(1.0,0.0)),u.x),
        mix(rand(ip+float2(0.0,1.0)),rand(ip+float2(1.0,1.0)),u.x),u.y);
    return res*res;
}

const mat2 mtx = mat2( 0.80,  0.60, -0.60,  0.80 );

float fbm( float2 p )
{
    float f = 0.0;

    f += 0.500000*noise( p + time  ); p = mtx*p*2.02;
    f += 0.031250*noise( p ); p = mtx*p*2.01;
    f += 0.250000*noise( p ); p = mtx*p*2.03;
    f += 0.125000*noise( p ); p = mtx*p*2.01;
    f += 0.062500*noise( p ); p = mtx*p*2.04;
    f += 0.015625*noise( p + sin(time) );

    return f/0.96875;
}

float pattern(float2 p )
{
	return fbm( p + fbm( p + fbm( p ) ) );
}

half4 main( float2 fragCoord )
{
    float2 uv = fragCoord/size.x;
	float shade = pattern(uv)*0.5;
    return half4(float3(shade),1);
}

"""

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

@Language("AGSL")
private const val SHADER1 = """
    
float NUM_LAYERS =10.0;
int ITER =23;
uniform float2 size;
uniform float time;

float4 tex(float3 p)
{
    float t = time+78.;
    float4 o = float4(p.xyz,3.*sin(t*.1));
    float4 dec = float4 (1.,.9,.1,.15) + float4(.06*cos(t*.1),0,0,.14*cos(t*.23));
    for (int i=0 ; i < 15;i++) o.xzyw = abs(o/dot(o,o)- dec);
    return o;
}

half4 main( float2 fragCoord )
{

    float2 uv = (fragCoord-size.xy*.5)/size.y;
    float3 col = float3(0);   
    float t= time* .3;
    
	for(float i=0.; i<=1.; i+=1./10.0)
    {
        float d = fract(i+t); // depth
        float s = mix(5.,.5,d); // scale
        float f = d * smoothstep(1.,.9,d); //fade
        col+= tex(float3(uv*s,i*4.)).xyz*f;
    }
    
    col/=10;
    col*=float3(2,1.,2.);
   	col=pow(col,float3(.5 ));  

    return half4(col,1.0);
}
"""

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

    @OptIn(ExperimentalHorologistApi::class)
    @Composable
    fun Page_1() {
        var time by remember { mutableFloatStateOf(0f) }
        val state = rememberScalingLazyListState()
        val scope = rememberCoroutineScope()

        ScalingLazyColumn(state = state, modifier = Modifier.rotaryWithScroll(state)) {
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
                            .fillMaxSize()
                            .clip(shape)
                            .weight(1f)
                            .background(primary_color)
                            .clickable {
                                startActivity(this@MainActivity, DataChannel::class.java)
                            }) {
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
                        Text(
                            text = "Script", modifier = Modifier

                                .fillMaxSize()
                                .weight(1f)
                                .background(surfaceColor, RoundedCornerShape(50))
                                .wrapContentHeight()
                                .clickable {
                                    startActivity(
                                        this@MainActivity, Scripts::class.java
                                    )
                                }, fontFamily = font1, textAlign = TextAlign.Center
                        )
                        Icon(imageVector = Icons.Rounded.PlayArrow, null, modifier = Modifier

                            .fillMaxSize()
                            .border(1.dp, primary_color, RoundedCornerShape(50))
                            .weight(1f)
                            .clickable { startNyx(this@MainActivity) })
                    }
                }
            }
            item {
                Icon(imageVector = Icons.TwoTone.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            startActivity(
                                this@MainActivity, DetailedActivity::class.java
                            )
                        })
            }
        }

        LaunchedEffect(key1 = time) {
            scope.launch {
                time +=  0.01f
                delay(10000)
            }
        }
    }

    private fun setupStorageSymlinks() {
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
        }

    }

}
