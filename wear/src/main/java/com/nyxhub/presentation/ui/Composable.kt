package com.nyxhub.presentation.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.nyxhub.presentation.font1
import com.nyxhub.presentation.primary_color
import com.nyxhub.presentation.surfaceColor
import com.termux.nyxhub.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language


@Language("AGSL")
private const val SHADER = """
uniform float2 size;
uniform float time;
float s(float a,float2 p) {
    return 1.0/length(p+float2(sin(a), cos(a)));
}

half4 main( float2 fragCord ){
    float2 p = (fragCord-size/2.0)/size.y*4.0;
    
    float d=s(time,p);
    
    for (float i=0;i<6.0;i+=1.256) 
    {
        d+=s(i-time,p);
    }
    
    d = d*0.75-6.0;
    
    return half4(d);
}
"""

@Composable
fun Loading() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        var time by remember { mutableFloatStateOf(0f) }
        val scope = rememberCoroutineScope()
        Box(modifier = Modifier
            .size(45.dp)
            .drawWithCache {
                val shader = RuntimeShader(SHADER)
                val shaderBrush = ShaderBrush(shader)
                shader.setFloatUniform("size", size.width, size.height)

                onDrawBehind {
                    shader.setFloatUniform("time", time)
                    drawRect(shaderBrush)
                }
            })
        LaunchedEffect(key1 = time) {
            scope.launch {
                time += 0.01f
                delay(100)
            }
        }
    } else {
        val alpha by rememberInfiniteTransition().animateFloat(
            initialValue = 1f, targetValue = 0.1f, animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000, delayMillis = 500
                ), repeatMode = RepeatMode.Reverse
            )
        )
        Canvas(
            modifier = Modifier
                .padding(10.dp)
                .size(30.dp)
        ) {
            drawCircle(
                color = primary_color,
                alpha = alpha,
                style = Stroke(width = 5f),
                radius = size.minDimension / 2 * (1 - alpha)
            )
        }
    }
}

@Composable
fun Button(modifier: Modifier? = null, icon: ImageVector, text: String, click: () -> Unit) {
    Row(
        modifier = (modifier ?: Modifier
            .fillMaxWidth()
            .background(surfaceColor, RoundedCornerShape(25))
            .padding(15.dp)).clickable { click() },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = primary_color)
        Text(text = text, fontFamily = font1, color = primary_color)
    }
}

@Composable
fun ButtonTransparent(modifier: Modifier=Modifier,icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .padding(5.dp)
            .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(50))
            .padding(10.dp)
            .fillMaxWidth(0.7f)
            .clickable { onClick(); },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, fontFamily = font1)
    }
}

@Composable
fun FailedScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)
    ) {
        Icon(painter = painterResource(id = R.drawable.network_error), contentDescription = null, modifier = Modifier.size(30.dp))
        Text(
            "Unable to connect to the server. Please try again later.",
            fontFamily = font1,
            textAlign = TextAlign.Center
        )
    }
}