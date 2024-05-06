package com.nyxhub.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.BlurOff
import androidx.compose.material.icons.twotone.BlurOn
import androidx.compose.material.icons.twotone.BorderOuter
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.FileOpen
import androidx.compose.material.icons.twotone.PhonelinkRing
import androidx.compose.material.icons.twotone.Replay
import androidx.compose.material.icons.twotone.Storage
import androidx.compose.material.icons.twotone.TextDecrease
import androidx.compose.material.icons.twotone.TextIncrease
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.nyxhub.file.FileChooser
import com.nyxhub.presentation.ui.Loading
import com.termux.shared.termux.NyxConstants.CONFIG_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

const val EXTRA_NORMAL_BACKGROUND: String = "$CONFIG_PATH/wallpaper.jpg"
const val EXTRA_BLUR_BACKGROUND: String = "$CONFIG_PATH/wallpaperBlur.jpg"
val primary_color =Color.White
class BackgroundManager : ComponentActivity() {
    private var enableBlur by mutableStateOf(true)
    private var enableBorder by mutableStateOf(true)
    private var wallpaper: ImageBitmap? by mutableStateOf(null)
    private var wallpaperBlur: ImageBitmap? by mutableStateOf(null)
    private var loading by mutableStateOf(false)
    private var popup by mutableStateOf(false)
    private var DetailedBlur by mutableFloatStateOf(.15f)
    private var font_size by mutableIntStateOf(14)
    private var density = 1f
    private fun loadValues() {
        val settingsMap = try {
            ObjectInputStream(FileInputStream("$CONFIG_PATH/configConsole")).use { it.readObject() as Map<String,Any> }
        } catch (e: Exception) {
            return
        }
        font_size = (settingsMap["font_size"] ?: font_size) as Int
    }
    private fun saveValues() {
        val map = mapOf("font_size" to font_size)
        ObjectOutputStream(FileOutputStream("$CONFIG_PATH/configConsole")).use { it.writeObject(map) }
    }
    @SuppressLint("InvalidFragmentVersionForActivityResult")
    @RequiresApi(Build.VERSION_CODES.S)
    private val result = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            CoroutineScope(Dispatchers.IO).launch {
                loading = true
                val bitmap = BitmapFactory.decodeFile(it.data?.getStringExtra("path"),
                    BitmapFactory.Options().apply { inMutable = true }).let { bitmap ->
                    val size = if (bitmap.width > bitmap.height) bitmap.height else bitmap.width
                    Bitmap.createBitmap(bitmap, 0, 0, size, size).scale(450, 450)
                }
                bitmap.save(EXTRA_NORMAL_BACKGROUND)
                blurBitmap(bitmap, DetailedBlur * 100).save(EXTRA_BLUR_BACKGROUND)
                loadWallpapers()
                loading = false
            }
        }
    }

    private fun loadWallpapers() {
        wallpaper = try {
            BitmapFactory.decodeFile(EXTRA_NORMAL_BACKGROUND).asImageBitmap()
        } catch (e: Exception) {
            null
        }
        wallpaperBlur = try {
            BitmapFactory.decodeFile(EXTRA_BLUR_BACKGROUND).asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        density=resources.displayMetrics.scaledDensity
        setContent {
            Ui()
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch { loadBool();loadWallpapers();loadValues() }
    }

    override fun onPause() {
        super.onPause()
        CoroutineScope(Dispatchers.IO).launch { saveBool();saveValues() }
    }


    private fun loadBool() {
        val settingsMap = try {
            ObjectInputStream(FileInputStream("$CONFIG_PATH/config")).use { it.readObject() as Map<String, Boolean> }
        } catch (e: Exception) {
            return
        }
        enableBorder = settingsMap["border"] ?: true
        enableBlur = settingsMap["blur"] ?: true
        DetailedBlur =
            getSharedPreferences("settings", MODE_PRIVATE).getFloat("DetailedBlur", 0.15f)
    }

    private fun saveBool() {
        try {
            ObjectOutputStream(FileOutputStream("$CONFIG_PATH/config")).use {
                it.writeObject(
                    mapOf(
                        "border" to enableBorder, "blur" to enableBlur
                    )
                )
            }
        } catch (e: Exception) {
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalHorologistApi::class)
    @Preview(device = Devices.WEAR_OS_SMALL_ROUND)
    @Composable
    private fun Ui() {
        val state = rememberScalingLazyListState()
        ScalingLazyColumn(
            state = state,
            modifier = Modifier
                .rotaryWithScroll(state)
                .fillMaxSize()
                .blur(if (popup || loading) 10.dp else 0.dp)
        ) {
            item { Text(text = "Wallpaper", fontFamily = font1) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(25))
                        .background(surfaceColor)
                        .fillMaxWidth()
                        .height(75.dp)
                        .clickable { popup = true }) {

                    if (wallpaper != null) Image(
                        modifier = Modifier.size(75.dp),
                        bitmap = wallpaper!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (wallpaperBlur != null && enableBlur) Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.4f),
                            bitmap = wallpaperBlur!!,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.TwoTone.FileOpen, contentDescription = null)
                            Text(text = "Choose", fontFamily = font1)
                        }

                    }
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Switch(
                        enable = enableBorder,
                        icon = Icons.TwoTone.BorderOuter,
                        text = "Border",
                        modifier = Modifier.weight(1f)
                    ) { enableBorder = !enableBorder }
                    var level by remember { mutableFloatStateOf(DetailedBlur) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(5.dp)
                            .clip(RoundedCornerShape(25))
                            .background(surfaceColor)
                            .aspectRatio(1f)
                            .weight(1f)
                            .clickable {
                                if (DetailedBlur != level && wallpaper != null) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        DetailedBlur = level
                                        if (DetailedBlur == 0f) {
                                            enableBlur = false;return@launch
                                        } else {
                                            blurBitmap(
                                                wallpaper!!.asAndroidBitmap(), DetailedBlur * 100
                                            ).save(
                                                EXTRA_BLUR_BACKGROUND
                                            )
                                            loadWallpapers()
                                            enableBlur = true
                                        }
                                        getSharedPreferences("settings", MODE_PRIVATE)
                                            .edit()
                                            .putFloat("DetailedBlur", DetailedBlur)
                                            .apply()
                                    }

                                }
                            }
                            .scrollable(orientation = Orientation.Vertical,
                                reverseDirection = true,
                                state = rememberScrollableState { delta ->
                                    level += delta / 150
                                    if (level < 0f) level = 0f
                                    else if (level > 1f) level = 1f
                                    delta
                                })
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    primary_color,
                                    size = size.copy(height = size.height * level),
                                    topLeft = Offset(0f, size.height * (1 - level)),
                                    blendMode = BlendMode.Exclusion
                                )
                            }) {
                        Icon(
                            imageVector = if (DetailedBlur != level) Icons.TwoTone.Replay else if (!enableBlur) Icons.TwoTone.BlurOff else Icons.TwoTone.BlurOn,
                            contentDescription = null,
                            tint = primary_color
                        )
                        Text(
                            text = "${(level * 100).toInt()}% Blur",
                            fontFamily = font1,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            color = primary_color
                        )

                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(surfaceColor, RoundedCornerShape(25))
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable {
                            CoroutineScope(Dispatchers.IO).launch {
                                loading = true
                                File(EXTRA_NORMAL_BACKGROUND).delete()
                                File(EXTRA_BLUR_BACKGROUND).delete()
                                loadWallpapers()
                                loading = false
                            }
                        }) {
                    Icon(
                        imageVector = Icons.TwoTone.Delete,
                        contentDescription = null,
                        tint = primary_color
                    )
                    Text(text = "Remove Wallpaper", fontFamily = font1)
                }
            }
            item { Text(text = "Text Size", fontFamily = font1) }
            item {
                Row(modifier = Modifier
                    .clip(RoundedCornerShape(25))
                    .fillMaxWidth()
                    .background(
                        surfaceColor
                    )
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            primary_color,
                            size = size.copy(width = size.width * font_size / 100),
                            blendMode = BlendMode.Exclusion
                        )
                    }
                    .padding(10.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (it.x > size.width / 2) {
                                if (font_size < 100)
                                    font_size++
                            } else {
                                if (font_size > 1)
                                    font_size--
                            }
                        }
                    }, horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        imageVector = Icons.TwoTone.TextDecrease,
                        contentDescription = null
                    )
                    Text(fontFamily = font1, text = "${font_size}sp", fontSize = (font_size/density).sp)
                    Icon(
                        imageVector = Icons.TwoTone.TextIncrease,
                        contentDescription = null
                    )
                }
            }
        }
        if (popup) Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(icon = Icons.TwoTone.PhonelinkRing, text = "Phone") {
                startActivity(Intent(
                    this@BackgroundManager, DataChannel::class.java
                ).apply {
                    putStringArrayListExtra(
                        "files", arrayListOf(EXTRA_NORMAL_BACKGROUND, EXTRA_BLUR_BACKGROUND)
                    )
                })
            }
            Button(icon = Icons.TwoTone.Storage, text = "Storage") {
                result.launch(Intent(this@BackgroundManager, FileChooser::class.java).apply {
                    putStringArrayListExtra(
                        "filters", arrayListOf("jpeg", "jpg", "png")
                    )
                })
            }
            Button(icon = Icons.TwoTone.Cancel, text = "Cancel")
        }
        if (loading) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Loading()
        }

    }

    @Composable
    fun Button(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
        Row(
            modifier = Modifier
                .padding(5.dp)
                .background(primary_color.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
                .padding(10.dp)
                .fillMaxWidth(.5f)
                .clickable { popup = false;onClick(); },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = text, fontFamily = font1)
        }
    }

    @Composable
    fun Switch(
        modifier: Modifier = Modifier,
        enable: Boolean,
        icon: ImageVector,
        text: String,
        onClick: () -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .padding(5.dp)
                .clip(RoundedCornerShape(25))
                .background(
                    color = if (enable) primary_color else surfaceColor,
                )
                .aspectRatio(1f)
                .clickable { onClick() }) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enable) surfaceColor else primary_color
            )
            Text(
                text = text,
                fontFamily = font1,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = if (enable) surfaceColor else Color.White
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun blurBitmap(bitmap: Bitmap, radius: Float = 15f): Bitmap {
        val imageReader = ImageReader.newInstance(
            bitmap.width,
            bitmap.height,
            PixelFormat.RGBA_8888,
            1,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
        )
        val renderNode = RenderNode("BlurEffect")
        val hardwareRenderer = HardwareRenderer()

        hardwareRenderer.setSurface(imageReader.surface)
        hardwareRenderer.setContentRoot(renderNode)
        renderNode.setPosition(0, 0, imageReader.width, imageReader.height)
        val blurRenderEffect = RenderEffect.createBlurEffect(
            radius, radius, Shader.TileMode.MIRROR
        )
        renderNode.setRenderEffect(blurRenderEffect)
        val renderCanvas = renderNode.beginRecording()
        renderCanvas.drawBitmap(bitmap, 0f, 0f, null)
        renderNode.endRecording()
        hardwareRenderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
        val image = imageReader.acquireNextImage() ?: throw RuntimeException("No Image")
        val hardwareBuffer = image.hardwareBuffer ?: throw RuntimeException("No HardwareBuffer")
        val outBitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
            ?: throw RuntimeException("Create Bitmap Failed")
        hardwareBuffer.close()
        image.close()
        imageReader.close()
        renderNode.discardDisplayList()
        hardwareRenderer.destroy()
        return outBitmap
    }

    private fun Bitmap.save(path: String) {
        this.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(path))
    }
}