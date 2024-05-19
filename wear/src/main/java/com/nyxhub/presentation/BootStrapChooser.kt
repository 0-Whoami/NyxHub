package com.nyxhub.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.RuntimeShader
import android.os.Build
import android.os.Bundle
import android.system.Os
import android.util.Pair
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material.icons.twotone.Download
import androidx.compose.material.icons.twotone.ExpandMore
import androidx.compose.material.icons.twotone.Web
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.nyxhub.nyx.FileChooser
import com.nyxhub.nyx.NyxConstants
import com.nyxhub.presentation.ui.AnimatedVisibility
import com.nyxhub.presentation.ui.Button
import com.nyxhub.presentation.ui.CardWithCaption
import com.nyxhub.presentation.ui.LazyList
import com.nyxhub.presentation.ui.Loading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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

class BootStrapChooser : ComponentActivity() {
    private var loading by mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private val result = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            scope.launch {
                val file = File(cacheDir, "temp.zip")
                File(it.data?.getStringExtra("path")).copyTo(file, true)
                file.inputStream().use { inp ->
                    val buff = BufferedInputStream(inp)
                    setupBootstrap(
                        buff
                    )
                }
                file.delete()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyList(blur = loading) {
                item { Text("Install Bootstrap", fontFamily = font1) }
                item {
                    var moreOptions by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(surfaceColor)
                    ) {
                        var time by remember { mutableFloatStateOf(0f) }
                        CardWithCaption(modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
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
                                    onDrawBehind {}
                                }
                            },
                            height = 80,
                            icon1 = Icons.Rounded.Watch,
                            text = "Patched",
                            subText = "Recommended lightweight Linux library sets for additional functionality:",
                            icon2 = Icons.TwoTone.ExpandMore,
                            icon2action = { moreOptions = !moreOptions }) {

                        }
                        LaunchedEffect(key1 = time) {
                            scope.launch {
                                time += 0.001f
                            }
                        }
                        if (moreOptions) {
                            CardWithCaption(
                                icon1 = Icons.Rounded.Storage,
                                text = "Storage",
                                subText = "Install bootstrap zip from internal storage"
                            ) {
                                result.launch(Intent(
                                    this@BootStrapChooser, FileChooser::class.java
                                ).apply {
                                    putStringArrayListExtra(
                                        "filters", arrayListOf("zip")
                                    )
                                })
                            }
                            CardWithCaption(
                                icon1 = Icons.TwoTone.Web,
                                text = "Link",
                                subText = "Install bootstrap from a valid link"
                            ) {

                            }
                        }
                    }
                }
                item {
                    var expand by remember { mutableStateOf(false) }
                    Button(
                        modifier = Modifier
                            .background(primary_color, RoundedCornerShape(25.dp))
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
                            scope.launch { setupBootstrap();finish() }
                        }
                    }
                }
                item {
                    var expand by remember { mutableStateOf(false) }
                    Text(modifier = Modifier
                        .clickable {
                            if (!expand) expand = true
                            else {
                                finish()
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
            AnimatedVisibility(visible = loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Loading()
                }
            }
        }
    }

    override fun finish() {
        setResult(RESULT_OK, Intent())
        super.finish()
    }

    private fun determineZipUrl(): String {
        return "https://github.com/termux/termux-packages/releases/latest/download/bootstrap-${determineTermuxArchName()}.zip"
    }

    private fun ensureDirectoryExists(directory: File): Boolean {
        return directory.exists().let { if (!it) directory.mkdirs() else true }
    }

    private fun String.deleteRecursively(): Boolean {
        return File(this).deleteRecursively()
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

    private fun setupBootstrap(input: InputStream = URL(determineZipUrl()).openStream()) {
        loading = true
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
            validateDir(NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH)
        ) {
            "Can't create ${NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH}"
        }
        // Create prefix directory if it does not already exist and set required permissions
        require(validateDir(NyxConstants.TERMUX_PREFIX_DIR_PATH)) {
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
                        val newPath = NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH + "/" + parts[1]
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
        applyPatch(this)
        loading = false
    }
}

fun validateDir(filepath: String): Boolean {
    val dir = File(filepath)
    return dir.exists()
        .let { if (!it) dir.mkdirs() else true } && dir.setReadable(true) && dir.setWritable(
        true
    ) && dir.setExecutable(true)
}

fun applyPatch(context: Context) = getData("$apiUrl/patch") { jsonArray ->
    environmentVariable()
    phraseJsonArray(context, jsonArray){}
}

const val ENV_FILE = "${NyxConstants.TERMUX_PREFIX_DIR_PATH}/etc/environment.sh"
fun environmentVariable() {
    val variables = mutableMapOf<String, String>()
    variables["HOME"] = NyxConstants.TERMUX_HOME_DIR_PATH
    variables["PWD"] = NyxConstants.TERMUX_HOME_DIR_PATH
    variables["LANG"] = "en_US.UTF-8"
    variables["PREFIX"] = NyxConstants.TERMUX_PREFIX_DIR_PATH
    variables["PATH"] = NyxConstants.TERMUX_BIN_PREFIX_DIR_PATH
    variables["TMPDIR"] = NyxConstants.TERMUX_TMP_PREFIX_DIR_PATH
    variables["COLORTERM"] = "truecolor"
    variables["TERM"] = "xterm-256color"
    variables["TERMUX_APP__FILES_DIR"] = NyxConstants.TERMUX_FILES_DIR_PATH
    variables["TERMUX_APP__PACKAGE_MANAGER"] = "apt"
    variables["TERMUX_MAIN_PACKAGE_FORMAT"] = "debian"
    for (i in listOf(
        "ANDROID_ASSETS",
        "ANDROID_DATA",
        "ANDROID_ROOT",
        "ANDROID_STORAGE",
        "EXTERNAL_STORAGE",
        "ASEC_MOUNTPOINT",
        "LOOP_MOUNTPOINT",
        "ANDROID_RUNTIME_ROOT",
        "ANDROID_ART_ROOT",
        "ANDROID_I18N_ROOT",
        "ANDROID_TZDATA_ROOT",
        "BOOTCLASSPATH",
        "DEX2OATBOOTCLASSPATH",
        "SYSTEMSERVERCLASSPATH"
    )) variables.putEnvVar(i)
    val stringBuilder = StringBuilder()
    variables.forEach { (name, value) ->
        stringBuilder.appendLine("export $name=$value")
    }
    File(
        ENV_FILE
    ).writeText(stringBuilder.toString())
}

private fun MutableMap<String, String>.putEnvVar(name: String) {
    with(System.getenv(name)) { if (this != null) this@putEnvVar[name] = this }
}