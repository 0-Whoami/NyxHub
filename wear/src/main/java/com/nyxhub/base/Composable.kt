package com.nyxhub.base

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.SwipeToReveal
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import com.nyxhub.R
import com.nyxhub.presentation.primary_color
import com.nyxhub.presentation.surfaceColor
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

val font = FontFamily(Font(R.font.mono))
val selColor = Color.White.copy(alpha = 0.05f)

@Composable
fun Card(text : String,
         click : () -> Unit = {},
         focused : Boolean = true,
         fontFam : FontFamily? = null,
         endingContent : @Composable () -> Unit = {},
         leadingContent : @Composable () -> Unit = {}) {
    val animPad by animateDpAsState(if (focused) 10.dp else 0.dp, label = "")
    Row(modifier = Modifier
        .padding(horizontal = animPad)
        .fillMaxWidth()
        .height(50.dp)
        .clickable(onClick = click)
        .selected(focused),
        verticalAlignment = Alignment.CenterVertically) {
        leadingContent()
        com.nyxhub.base.Text(text, modifier = Modifier.padding(15.dp), fontFamily = fontFam ?: font)
        Spacer(Modifier.weight(1f))
        endingContent()
    }
}

@Composable
fun Loading(modifier : Modifier = Modifier) {
    val angle by rememberInfiniteTransition("").animateFloat(2.356f, 5.498f, infiniteRepeatable(tween()), "")
    Canvas(modifier
               .size(50.dp)
               .padding(5.dp)) {
        val x = size.minDimension / 2
        drawCircle(primary_color, radius = x - 10, style = Stroke(8f))
        drawLine(primary_color,
                 start = Offset(x * (1 + cos(angle)), x * (1 + sin(angle))),
                 end = Offset(x * (1 + cos(3.14f + angle)), x * (1 + sin(3.14f + angle))),
                 10f,
                 blendMode = BlendMode.Clear)
    }
}

@Composable
fun Modifier.selected(isIt : Boolean, lineLength : Float = 15f) =
    this.then(if (isIt) Modifier
        .cornerBorder(lineLength)
        .background(selColor) else Modifier)

fun Modifier.cornerDotBorder(radius : Float = 1.5f) = this.then(Modifier.drawWithContent {
    drawContent()
    val cornerOffsets = listOf(Offset.Zero, Offset(size.width, 0f), Offset(0f, size.height), Offset(size.width, size.height))
    drawRect(primary_color.copy(alpha = 0.5f), style = Stroke())
    cornerOffsets.forEach { offset ->
        drawCircle(primary_color, radius, offset)
    }
})


@Composable
fun Modifier.cornerBorder(lineLength : Float = 15f) : Modifier {
    val path = remember { Path() }
    return this.then(Modifier
                         .onSizeChanged { size ->
                             val width = size.width.toFloat()
                             val height = size.height.toFloat()
                             path.reset()
                             path.apply {
                                 moveTo(0f, lineLength)
                                 lineTo(0f, 0f)
                                 lineTo(lineLength, 0f)
                                 moveTo(width - lineLength, 0f)
                                 lineTo(width, 0f)
                                 lineTo(width, lineLength)
                                 moveTo(width, height - lineLength)
                                 lineTo(width, height)
                                 lineTo(width - lineLength, height)
                                 moveTo(lineLength, height)
                                 lineTo(0f, height)
                                 lineTo(0f, height - lineLength)
                             }
                         }
                         .drawWithCache {
                             onDrawWithContent {
                                 drawContent()
                                 drawPath(path, primary_color, style = Stroke(width = 1.dp.toPx()))
                             }
                         })
}


@Composable
fun LazyListWrapper(ci : Int = 1,
                    columnState : ScalingLazyListState = rememberScalingLazyListState(ci),
                    content : ScalingLazyListScope.(Int) -> Unit) {
    val scalingParams = remember { ScalingLazyColumnDefaults.scalingParams(edgeScale = 1f, edgeAlpha = 1f) }
    ScalingLazyColumn(
        state = columnState,
        autoCentering = AutoCenteringParams(itemIndex = 0),
        verticalArrangement = Arrangement.Center,
        scalingParams = scalingParams,
        flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(columnState),
        rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState),
    ) {
        content(columnState.centerItemIndex - 2)
    }

}

@Composable
fun Switch(label : String = "Example", check : Boolean = true, focused : Boolean = false, onClick : (Boolean) -> Unit = {}) {
    Card(text = label, focused = focused, click = { onClick(!check) }, endingContent = {
        val offset by animateDpAsState(if (check) 10.dp else 0.dp, label = "")
        Box(modifier = Modifier
            .padding(10.dp)
            .border(1.dp, primary_color)
            .size(24.dp, 9.dp)
            .padding(2.dp)
            .padding(start = offset)) {
            Box(Modifier
                    .size(10.dp, 5.dp)
                    .background(primary_color))
        }
    })
}

@Composable
fun TextValue(text : String, value : String, focused : Boolean = false, onClick : () -> Unit = {}) {
    Card(text = text, click = onClick, focused = focused, endingContent = {
        com.nyxhub.base.Text(value, modifier = Modifier.padding(10.dp))
    })
}

@Composable
fun Heading(text : String) {
    com.nyxhub.base.Text(text, modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp), textAlign = TextAlign.Start, fontSize = 20.sp)
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier
        .size(1.dp, 50.dp)
        .background(primary_color))
}

@Preview
@Composable
fun NotifyingAnimation(text : String = "APPLIED!", enable : Boolean = true, onFinished : () -> Unit = {}) {
    val animatedWidth by animateFloatAsState(if (enable) 1f else 0f,
                                             animationSpec = tween(1000, easing = FastOutLinearInEasing),
                                             label = "",
                                             finishedListener = { if (it == 1f) onFinished() })
    com.nyxhub.base.Text(text,
                         color = surfaceColor,
                         modifier = Modifier
                             .fillMaxWidth(animatedWidth)
                             .height(50.dp)
                             .background(primary_color)
                             .padding(10.dp)
                             .wrapContentHeight())

}

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun SwappableCard(deleteAction : () -> Unit, editAction : () -> Unit, composable : @Composable () -> Unit) {
    val revealState = rememberRevealState()
    val coroutine = rememberCoroutineScope()
    SwipeToReveal(primaryAction = {
        com.nyxhub.base.Text("DELETE",
                             Modifier
                                 .background(primary_color)
                                 .clickable {
                                     coroutine.launch {
                                         revealState.animateTo(RevealValue.Revealed);deleteAction();revealState.animateTo(RevealValue.Covered)
                                     }
                                 }
                                 .fillMaxSize()
                                 .wrapContentSize(), softWrap = false, color = surfaceColor)
    }, state = revealState, onFullSwipe = deleteAction, secondaryAction = {
        com.nyxhub.base.Text("EDIT",
                             Modifier
                                 .background(primary_color)
                                 .clickable { editAction();coroutine.launch { revealState.animateTo(RevealValue.Covered) } }
                                 .fillMaxSize()
                                 .wrapContentSize(),
                             softWrap = false,
                             color = surfaceColor)
    }, content = composable)
}

@Composable
fun time(format : String = "hh:mm") = TimeTextDefaults.timeSource(format).currentTime

@Composable
fun Text(text : String = time(),
         modifier : Modifier = Modifier,
         color : Color = Color.Unspecified,
         fontSize : TextUnit = TextUnit.Unspecified,
         fontStyle : FontStyle? = null,
         fontWeight : FontWeight? = null,
         fontFamily : FontFamily? = font,
         letterSpacing : TextUnit = TextUnit.Unspecified,
         textDecoration : TextDecoration? = null,
         textAlign : TextAlign? = TextAlign.Center,
         lineHeight : TextUnit = TextUnit.Unspecified,
         overflow : TextOverflow = TextOverflow.Ellipsis,
         softWrap : Boolean = true,
         maxLines : Int = Int.MAX_VALUE,
         minLines : Int = 1,
         onTextLayout : (TextLayoutResult) -> Unit = {},
         style : TextStyle = LocalTextStyle.current) {
    Text(AnnotatedString(text),
         modifier,
         color,
         fontSize,
         fontStyle,
         fontWeight,
         fontFamily,
         letterSpacing,
         textDecoration,
         textAlign,
         lineHeight,
         overflow,
         softWrap,
         maxLines,
         minLines,
         emptyMap(),
         onTextLayout,
         style)
}

@Composable
fun TextField(text : String, onValueChanged : (String) -> Unit, keyboardType : KeyboardType = KeyboardType.Text, readOnly : Boolean = false) {
    val interAction = remember { MutableInteractionSource() }
    BasicTextField(
        text,
        onValueChanged,
        interactionSource = interAction,
        modifier = Modifier.selected(interAction.collectIsFocusedAsState().value, 10f),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = keyboardType),
        cursorBrush = SolidColor(primary_color),
        readOnly = readOnly,
        textStyle = TextStyle(fontFamily = font, color = primary_color, textAlign = TextAlign.Center),
    )
}