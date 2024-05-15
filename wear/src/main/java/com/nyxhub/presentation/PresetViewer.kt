package com.nyxhub.presentation

import android.graphics.BitmapFactory
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import android.system.Os
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.ExpandMore
import androidx.compose.material.icons.twotone.Watch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.nyxhub.file.FileUtils
import com.nyxhub.presentation.ui.AnimatedVisibility
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.FailedScreen
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.presentation.ui.Loading
import com.termux.shared.termux.NyxConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Language("AGSL")
private const val SHADER = """
    uniform float2 size;
    uniform float time;
    
    mat2 m(float a){float c=cos(a), s=sin(a);return mat2(c,-s,s,c);}
float map(float3 p){
    p.xz*= m(time*0.2);p.xy*= m(time*0.3);
    float3 q = p*2.+time;
    return length(p+float3(sin(time*0.7)))*log(length(p)+1.) + sin(q.x+sin(q.z+sin(q.y)))*2.3 - 1.;
}

half4 main(float2 fragCoord ){	
	float2 p = fragCoord/size - float2(.9,.5);
    float3 cl = float3(0.);
    float d = 2.5;
    for(int i=0; i<=5; i++)	{
		float3 p = float3(0,0,5.) + normalize(float3(p, -1.))*d;
        float rz = map(p);
		float f =  clamp((rz - map(p+.1))*0.5, -.1, 1. );
        float3 l = float3(0.1,0.3,.4) + float3(5., 2.5, 3.)*f;
        cl = cl*l + smoothstep(2.5, .0, rz)*.7*l;
		d += min(rz, 1.);
	}
    return half4(cl, 1.);
}
"""

@Language("AGSL")
private const val BLUR_SHADER = """
uniform float2 resolution;
uniform shader contents;

vec4 main(in vec2 fragCoord) {
    float2 uv = fragCoord.xy / resolution.xy;
    vec4 color = vec4(0.0);
    float totalWeight = 0.0;
    float blurRadius = 5.0; // Adjust this value to control the blur strength

    for (float x = -blurRadius; x <= blurRadius; x++) {
        for (float y = -blurRadius; y <= blurRadius; y++) {
            float2 offset = float2(x, y) / resolution.xy;
            float weight = 1.0 / (1.0 + length(offset)); // Gaussian-like weight function
            color += contents.eval(uv + offset) * weight;
            totalWeight += weight;
        }
    }

    return color / totalWeight;
}
"""

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
const val skippingCaution =
    "Caution: Skipping the bootstrap installation will prevent you from using any Termux packages. It is highly recommended to install the bootstrap if you have not done so already. Click again to confirm skipping the installation."
const val installCaution =
    "Caution: Proceeding with the installation may wipe existing data. Ensure that you have backed up any important data before continuing. Click again to confirm you understand and wish to proceed with the installation."

class Image(val name: String, val src: ImageBitmap)
class PresetViewer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val urlOfFlavour = intent.getStringExtra("url")!!
        val prev = mutableStateListOf<Image>()
        val scope = CoroutineScope(Dispatchers.IO)
        var networkState by mutableStateOf(NetWorkResponse.Loading)
        var descriptions by mutableStateOf("")
        scope.launch {
            networkState = getData(urlOfFlavour) { jsonArray ->
                for (i in 0..<jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    if (obj.getString("name") == "ABOUT.md") {
                        async {
                            download(obj.getString("download_url")) { inp ->
                                val reader = BufferedReader(InputStreamReader(inp))
                                val response = StringBuilder()

                                var line: String?
                                while (reader.readLine().also { line = it } != null) {
                                    response.append(line)
                                }
                                reader.close()
                                descriptions = response.toString()
                            }
                        }
                    }
                    if (obj.getString("name") == "res" && obj.getString(
                            "type"
                        ) == "dir"
                    ) {
                        networkState = NetWorkResponse.Loading
                        async {
                            networkState = getData(obj.getString("url")) { inJson ->
                                for (j in 0..<inJson.length()) {
                                    val obj2 = inJson.getJSONObject(j)
                                    download(obj2.getString("download_url")) { inp ->
                                        try {
                                            prev.add(
                                                Image(
                                                    obj2.getString("name").split(".")[0],
                                                    BitmapFactory.decodeStream(inp).asImageBitmap()
                                                )
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        setContent {
            val state =
                rememberPagerState { prev.size.plus(if (networkState == NetWorkResponse.Loading) 1 else 0) }
            var bootstrap by remember { mutableStateOf(false) }
            LazyList(
                blur = bootstrap, contentPadding = PaddingValues(0.dp)
            ) {
                item { Text(text = "Preview", fontFamily = font1) }
                item {
                    if (networkState == NetWorkResponse.Failed) FailedScreen()
                    else {
                        var showName by remember { mutableStateOf(false) }
                        HorizontalPager(state = state) {
                            if (it == prev.size) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) { Loading() }
                            } else {
                                val textWidth = rememberTextMeasurer()
                                Image(bitmap = prev[it].src,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clickable { showName = !showName }
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .drawWithCache {
                                            val measure = textWidth.measure(
                                                prev[it].name, style = TextStyle(
                                                    color = primary_color, fontFamily = font1
                                                )
                                            )
                                            onDrawWithContent {
                                                drawContent()
                                                if (showName) {
                                                    drawRect(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color.Transparent, Color.Black
                                                            )
                                                        )
                                                    )
                                                    drawText(
                                                        textLayoutResult = measure,
                                                        topLeft = Offset(
                                                            (size.width - measure.size.width) / 2,
                                                            size.height - (measure.size.height + 20)
                                                        )
                                                    )
                                                }
                                            }
                                        })


                            }
                        }

                    }
                }
                item {

                }
                item {
                    Button(
                        modifier = Modifier
                            .background(
                                primary_color, RoundedCornerShape(50)
                            )
                            .padding(10.dp),
                        color = surfaceColor,
                        icon = Icons.TwoTone.Download,
                        text = "Install"
                    ) { bootstrap = true }
                }
                item {
                    Text(
                        text = descriptions,
                        fontFamily = font1,
                        color = primary_color.copy(alpha = 0.7f)
                    )
                }

            }

            AnimatedVisibility(visible = bootstrap) {
                LazyList {
                    item { Text("Install Bootstrap", fontFamily = font1) }
                    item {
                        var time by remember { mutableFloatStateOf(0f) }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(25))
                                .drawWithCache {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val shader = RuntimeShader(SHADER3)
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
                                }) {
                            Icon(
                                imageVector = Icons.TwoTone.Watch,
                                contentDescription = null,
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    text = "Patched",
                                    fontFamily = font1,
                                    style = MaterialTheme.typography.body2
                                )
                                Text(
                                    text = "Lightweight sets of linux libraries for extra functionality.(Recommend)",
                                    fontFamily = font1,
                                    fontSize = 8.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.TwoTone.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        LaunchedEffect(key1 = time) {
                            scope.launch {
                                time += 0.001f
                            }
                        }
                    }
                    item {
                        var expand by remember { mutableStateOf(false) }
                        Button(
                            modifier = Modifier
                                .background(primary_color, RoundedCornerShape(50))
                                .padding(10.dp)
                                .animateContentSize()
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            color = surfaceColor,
                            icon = Icons.TwoTone.Download,
                            text = if (expand) installCaution else "Install"
                        ) {
                            if (!expand) expand = true
                            else {

                            }
                        }
                    }
                    item {
                        var expand by remember { mutableStateOf(false) }
                        Text(modifier = Modifier
                            .clickable {
                                if (!expand) expand = true
                                else {
                                    bootstrap = false
                                }
                            }
                            .padding(10.dp)
                            .animateContentSize()
                            .fillMaxWidth()
                            .wrapContentWidth(),
                            textDecoration = if (expand) TextDecoration.None else TextDecoration.Underline,
                            text = if (expand) skippingCaution else "Skip")

                    }
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
                "arm64-v8a" -> return "aarch64"
                "armeabi-v7a" -> return "arm"
                "x86_64" -> return "x86_64"
                "x86" -> return "i686"
            }
        }
        return ""
    }

    private fun ensureDirectoryExists(directory: File): Boolean {
        return FileUtils.createDirectoryFile(directory.absolutePath)
    }

    private fun String.deleteRecursively(): Boolean {
        return File(this).deleteRecursively()
    }

    private fun setupBootstrap(input: InputStream) {

        try {

            require(NyxConstants.TERMUX_FILES_DIR_PATH.deleteRecursively()) { "Can't delete ${NyxConstants.TERMUX_FILES_DIR_PATH}" }

            // Delete prefix staging directory or any file at its destination

            require(NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH.deleteRecursively()) {
                "Can't delete staging ${NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH}"
            }
            // Delete prefix directory or any file at its destination
            require(NyxConstants.TERMUX_PREFIX_DIR_PATH.deleteRecursively()) {
                "Can't delete ${NyxConstants.TERMUX_PREFIX_DIR_PATH}"
            }
            // Create prefix staging directory if it does not already exist and set required permissions

            require(
                FileUtils.isTermuxPrefixStagingDirectoryAccessible(
                    createDirectoryIfMissing = true, setMissingPermissions = true
                )
            ) {
                "Can't create ${NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH}"
            }
            // Create prefix directory if it does not already exist and set required permissions

            if (FileUtils.isTermuxPrefixDirectoryAccessible(
                    createDirectoryIfMissing = true, setMissingPermissions = true
                )
            ) {
                "Can't create ${NyxConstants.TERMUX_PREFIX_DIR_PATH}"
            }
            val buffer = ByteArray(8096)
            val symlinks: MutableList<Pair<String, String>> = ArrayList(50)
            ZipInputStream(input).use { zipInput ->
                var zipEntry: ZipEntry?
                while (zipInput.nextEntry.also { zipEntry = it } != null) {
                    if (zipEntry!!.name == "SYMLINKS.txt") {
                        val symlinksReader = BufferedReader(InputStreamReader(zipInput))
                        var line: String?
                        while (symlinksReader.readLine().also { line = it } != null) {
                            val parts = line!!.split("←".toRegex()).dropLastWhile { it.isEmpty() }
                            require(parts.size == 2) {
                                "Malformed symlink line: $line"
                            }

                            val oldPath = parts[0]
                            val newPath =
                                NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH + "/" + parts[1]
                            symlinks.add(
                                Pair.create(
                                    oldPath, newPath
                                )
                            )
                            require(
                                ensureDirectoryExists(
                                    File(newPath).parentFile!!
                                )
                            ) {
                                "Can't create symlink directory, $newPath"
                            }
                        }
                    } else {
                        val zipEntryName = zipEntry!!.name
                        val targetFile = File(
                            NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH, zipEntryName
                        )
                        val isDirectory = zipEntry!!.isDirectory
                        require(ensureDirectoryExists(if (isDirectory) targetFile else targetFile.parentFile)) {
                            "Can't create symlink directory, $targetFile"
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
            require(symlinks.isNotEmpty()) {
                "No Symlink text"
            }
            for (symlink in symlinks) {
                Os.symlink(symlink.first, symlink.second)
            }
            require(NyxConstants.TERMUX_STAGING_PREFIX_DIR.renameTo(NyxConstants.TERMUX_PREFIX_DIR)) {
                "Moving termux prefix staging to prefix directory failed"
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

    }
}