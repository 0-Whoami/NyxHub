package com.nyxhub.presentation

import android.graphics.BitmapFactory
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import android.system.Os
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Download
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import com.nyxhub.file.FileUtils
import com.nyxhub.presentation.ui.ButtonTransparent
import com.nyxhub.presentation.ui.FailedScreen
import com.nyxhub.presentation.ui.Loading
import com.termux.shared.termux.NyxConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
private const val SHADER4 = """
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

class PresetViewer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val urlOfFlavour = intent.getStringExtra("url")!!
        val prevs = mutableStateListOf<ImageBitmap>()
        val scope = CoroutineScope(Dispatchers.IO)
        var networkState by mutableStateOf(NetWorkResponse.Loading)
        scope.launch {
            networkState = getData(urlOfFlavour) {
                for (i in 0..<it.length()) {
                    val obj = it.getJSONObject(i)
                    if (obj.getString("name") == "src" && obj.getString("type") == "dir") {
                        getData(obj.getString("url")) { inJson ->
                            for (j in 0..<inJson.length()) {
                                val obj2 = inJson.getJSONObject(j)
                                download(obj2.getString("download_url"), {}) { inp ->
                                    prevs.add(BitmapFactory.decodeStream(inp).asImageBitmap())
                                }
                            }
                        }
                    }

                }
            }
        }

        setContent {
            val state =
                rememberPagerState { prevs.size.plus(if (networkState == NetWorkResponse.Loading) 1 else 0) }
            var x by remember { mutableFloatStateOf(0f) }
            var y by remember { mutableFloatStateOf(0f) }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (networkState == NetWorkResponse.Failed) FailedScreen()
                else HorizontalPager(state = state) {
                    if (it == prevs.size) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) { Loading() }
                    } else {
                        Image(
                            bitmap = prevs[it],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                ButtonTransparent(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned {
                            x = it.positionInParent().x;y = it.positionInParent().y
                        },
                    icon = Icons.TwoTone.Download,
                    text = "Install"
                )
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
        CoroutineScope(Dispatchers.IO).launch {
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
                                val parts =
                                    line!!.split("←".toRegex()).dropLastWhile { it.isEmpty() }
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
                if (!NyxConstants.TERMUX_STAGING_PREFIX_DIR.renameTo(NyxConstants.TERMUX_PREFIX_DIR)) {
                    throw RuntimeException("Moving termux prefix staging to prefix directory failed")
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }
}