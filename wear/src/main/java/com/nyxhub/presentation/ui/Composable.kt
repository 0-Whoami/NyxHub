package com.nyxhub.presentation.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import com.nyxhub.presentation.font1
import com.nyxhub.presentation.primary_color
import com.nyxhub.presentation.surfaceColor
import com.termux.nyxhub.R


@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun LazyList(
    modifier: Modifier = Modifier,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    blur: Boolean = false,
    anchorType: ScalingLazyListAnchorType = ScalingLazyListAnchorType.ItemCenter,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    content: ScalingLazyListScope.() -> Unit
) {
    ScalingLazyColumn(
        modifier = modifier
            .blur(if (blur) 15.dp else 0.dp)
            .fillMaxSize()
            .rotaryScrollable(RotaryScrollableDefaults.behavior(state),
                rememberActiveFocusRequester()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = state,
        anchorType = anchorType,
        contentPadding = contentPadding,
        content = content
    )
    PositionIndicator(scalingLazyListState = state)
}

@Composable
fun CardWithCaption(
    modifier: Modifier=Modifier,
    height:Int=45,
    icon1: ImageVector,
    text: String,
    subText: String,
    icon2: ImageVector? = null,
    icon2action: () -> Unit = {},
    click: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .clickable(indication = null, interactionSource = null) { click() }
            .fillMaxWidth()
            .height(height.dp)
            .then(modifier)) {
        Icon(
            imageVector = icon1, contentDescription = null, modifier = Modifier.weight(1f)
        )
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = text, fontFamily = font1, style = MaterialTheme.typography.body2
            )
            Text(
                text = subText, fontFamily = font1, fontSize = 8.sp
            )
        }
        if (icon2 != null) Icon(
            imageVector = icon2,
            contentDescription = null,
            modifier = Modifier
                .clickable { icon2action() }
                .weight(1f)
                .clip(
                    CircleShape
                )
        )
    }
}

@Composable
fun Loading() {

    val alpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 1f, targetValue = 0.1f, animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000, delayMillis = 500
            ), repeatMode = RepeatMode.Reverse
        ), label = ""
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

@Composable
fun AnimatedVisibility(
    visible: Boolean, content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    androidx.compose.animation.AnimatedVisibility(visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        content = content
    )
}

@Composable
fun Button(
    modifier: Modifier? = null,
    icon: ImageVector,
    color: Color = primary_color,
    background:Color= surfaceColor,
    text: String,
    click: () -> Unit
) {
    Row(modifier = Modifier
        .clickable(indication = null, interactionSource = null) { click() }
        .then(
            modifier ?: Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(25))
                .background(background)
                .padding(15.dp)
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Text(text = text, fontFamily = font1, color = color)
    }
}

@Composable
fun ButtonTransparent(icon: ImageVector, text: String, onClick: () -> Unit = {}) {
    Button(
        modifier = Modifier
            .padding(5.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.3f))
            .padding(10.dp)
            .fillMaxWidth(0.5f),
        icon = icon,
        text = text
    ) {
        onClick()
    }
}

@Composable
fun FailedScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.network_error),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
        Text(
            "Unable to connect to the server. Please try again later.",
            fontFamily = font1,
            textAlign = TextAlign.Center
        )
    }
}