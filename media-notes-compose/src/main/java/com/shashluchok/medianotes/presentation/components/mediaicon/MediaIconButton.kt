package com.shashluchok.medianotes.presentation.components.mediaicon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.presentation.components.CustomRippleColor

private val iconSize = 24.dp
private const val disabledIconScale = 0.85f
private const val iconScaleAnimationDurationMillis = 200

internal object MediaIconButtonDefaults {

    private const val DISABLED_ICON_OPACITY = 0.38f

    @Composable
    fun iconButtonColors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Unspecified,
        disabledContentColor: Color = contentColor.copy(alpha = DISABLED_ICON_OPACITY)
    ) = IconButtonDefaults.iconButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}

@Composable
internal fun MediaIconButton(
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    colors: IconButtonColors = MediaIconButtonDefaults.iconButtonColors()
) {
    CustomRippleColor(
        color = colors.contentColor
    ) {
        val scale by animateFloatAsState(
            targetValue = if (enabled) 1f else disabledIconScale,
            animationSpec = tween(durationMillis = iconScaleAnimationDurationMillis)
        )

        IconButton(
            modifier = modifier.scale(scale),
            onClick = onClick,
            enabled = enabled,
            colors = colors
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painter,
                contentDescription = contentDescription
            )
        }
    }
}
