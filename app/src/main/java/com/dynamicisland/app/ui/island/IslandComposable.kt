package com.dynamicisland.app.ui.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dynamicisland.app.data.model.IslandNotification
import com.dynamicisland.app.data.model.IslandSettings

/**
 * Renderiza la isla según su estado actual. El tamaño anima con un
 * spring (efecto elástico tipo Apple) cuya rigidez depende de
 * `animationSpeed` definido por el usuario en ajustes.
 */
@Composable
fun IslandComposable(
    state: IslandState,
    settings: IslandSettings,
    onTap: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = state is IslandState.Expanded
    val isHidden = state is IslandState.Hidden

    val targetWidth = when {
        isHidden -> 0.dp
        isExpanded -> settings.expandedWidthDp.dp
        else -> settings.collapsedWidthDp.dp
    }
    val targetHeight = when {
        isHidden -> 0.dp
        isExpanded -> settings.expandedHeightDp.dp
        else -> settings.collapsedHeightDp.dp
    }

    // Rigidez del spring: a mayor animationSpeed, más "snappy" el rebote.
    val stiffness = (Spring.StiffnessMediumLow * settings.animationSpeed).coerceIn(80f, 2500f)

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = stiffness
        ),
        label = "islandWidth"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = stiffness
        ),
        label = "islandHeight"
    )

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(animatedHeight)
            .clip(RoundedCornerShape(settings.cornerRadiusDp.dp))
            .background(Color.Black.copy(alpha = settings.backgroundAlpha))
            .clickable(enabled = isExpanded, onClick = onTap)
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(animationSpec = spring(stiffness = stiffness))
                    .togetherWith(fadeOut(animationSpec = spring(stiffness = stiffness)))
            },
            label = "islandContent"
        ) { targetState ->
            when (targetState) {
                is IslandState.Expanded -> ExpandedContent(
                    notification = targetState.notification,
                    onDismiss = onDismiss
                )
                else -> Spacer(modifier = Modifier.fillMaxHeight())
            }
        }
    }
}

@Composable
private fun ExpandedContent(
    notification: IslandNotification,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        AppIcon(notification)

        Spacer(modifier = Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            NotificationTexts(notification)
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cerrar",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AppIcon(notification: IslandNotification) {
    val bitmap = remember(notification.icon) {
        notification.icon?.toBitmap(width = 96, height = 96)
    }
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = notification.appName,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun NotificationTexts(notification: IslandNotification) {
    Column {
        Text(
            text = notification.title.ifBlank { notification.appName },
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = notification.text,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
