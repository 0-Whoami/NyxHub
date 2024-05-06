package com.nyxhub.presentation.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.nyxhub.presentation.font1
import com.nyxhub.presentation.primary_color
import com.nyxhub.presentation.surfaceColor
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

var previous: FloatArray = floatArrayOf(.5f, .7f, .6f, .8f)
@Composable
fun Cell(
    modifier: Modifier = Modifier, icon: ImageVector, text: String, onClick: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(5.dp)
            .background(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(25))
            .padding(5.dp)
            .aspectRatio(1f)
            .clickable { onClick() }) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, fontFamily = font1, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}
@Composable
fun GradientAnimation(
    modifier: Modifier = Modifier, randomnessRange: FloatArray, randomLength: IntArray
) {
    val coroutineScope = rememberCoroutineScope()
    val stars = remember {
        val list = mutableStateListOf<Star>()
        coroutineScope.launch {
            repeat(Random.nextInt(50, 500)) {
                val r=Random.nextFloat()*Random.nextInt(5)
                list.add(
                    Star(
                        Offset(Random.nextFloat(),Random.nextFloat()),
                        Size(r,r),
                        Random.nextFloat() * 2 * 3.14f
                    )
                )
            }
        }
        list
    }
    val transition = rememberInfiniteTransition("gradient")
    val point1 by transition.animateFloat(
        initialValue = previous[0],
        targetValue = randomnessRange[0],
        animationSpec = infiniteRepeatable(
            tween(randomLength[0]), repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )
    val point2 by transition.animateFloat(
        initialValue = previous[1],
        targetValue = randomnessRange[1],
        animationSpec = infiniteRepeatable(
            tween(randomLength[1]), repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )
    val point3 by transition.animateFloat(
        initialValue = previous[2],
        targetValue = randomnessRange[2],
        animationSpec = infiniteRepeatable(
            tween(randomLength[2]), repeatMode = RepeatMode.Reverse
        ),
        label = "p3"
    )
    val point4 by transition.animateFloat(
        initialValue = previous[3],
        targetValue = randomnessRange[3],
        animationSpec = infiniteRepeatable(
            tween(randomLength[3]), repeatMode = RepeatMode.Reverse
        ),
        label = "p4"
    )
    val starAlpha by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(6000), repeatMode = RepeatMode.Reverse
        ),
        label = "stars"
    )
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawRect(Color.Black)
        drawRect(
            Brush.radialGradient(
                listOf(
                    Color(0xff0b0b61).copy(0.3f * point3), Color.Transparent
                ), Offset(size.width * .24f * point1, 0.01f * size.height * point2)
            )
        )
        drawRect(
            Brush.radialGradient(
                listOf(
                    Color(0xff191970).copy(0.3f * point2), Color.Transparent
                ), Offset(.01f * size.width * point1, size.height * .99f * point2)
            )
        )
        drawRect(
            Brush.radialGradient(
                listOf(
                    Color(0xff3f397d).copy(alpha = .3f * point4), Color.Transparent
                ), Offset(size.width * .99f * point3, size.height * .02f * point1)
            )
        )
        drawRect(
            Brush.radialGradient(
                listOf(
                    Color(0xff4c3f91).copy(.3f * point1), Color.Transparent
                ), Offset(size.width * .44f * point2, size.height * point4)
            )
        )

            for (i in stars) {
                i.update(starAlpha)
            }

        drawStars(stars)
    }
    DisposableEffect(randomLength) {
        onDispose {
            previous = floatArrayOf(point1, point2, point3, point4)
        }
    }
}

private fun DrawScope.drawStars(stars: List<Star>) {
    stars.forEach { star ->
        drawRect(
            color = Color(0xffC0c0c0),
            size = star.radius,
            topLeft = star.offset*size.height,
            alpha = star.twinkle
        )
    }
}

data class Star(
    val offset: Offset, val radius: Size, var twinkle: Float
) {
    private val initialAlpha = twinkle
    fun update(value: Float) {
        val x = (value - initialAlpha).toDouble()
        val newAlpha = 0.5f + (0.5f * sin(x).toFloat())
        twinkle = newAlpha
    }
}


@Composable
fun Loading() {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000, delayMillis = 500), repeatMode = RepeatMode.Reverse)
    )
    Canvas(modifier = Modifier.padding(10.dp).size(30.dp)) {
        drawCircle(color = primary_color, alpha = alpha, style = Stroke(width = 5f))
    }
}
@Composable
fun Button(modifier: Modifier=Modifier,icon:ImageVector,color: Boolean=false,text:String,click:()->Unit){
    Row(modifier = modifier
        .fillMaxWidth()
        .background(if (color) primary_color else surfaceColor, RoundedCornerShape(25))
        .padding(15.dp)
        .clickable { click() }, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = if (color) surfaceColor else primary_color)
        androidx.compose.material3.Text(text = text, fontFamily = font1, color =if (color) surfaceColor else primary_color)
    }
}