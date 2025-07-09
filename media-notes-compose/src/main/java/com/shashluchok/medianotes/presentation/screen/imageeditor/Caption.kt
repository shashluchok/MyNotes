package com.shashluchok.medianotes.presentation.screen.imageeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.components.keyboard.rememberKeyboardTransitionState
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButton
import com.shashluchok.medianotes.presentation.components.mediaicon.MediaIconButtonDefaults

private val captionBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color.Transparent,
        1f to Color.Black
    )
)

private val captionDefaultOuterPadding = 16.dp
private const val captionDefaultCornerRadius = 50f
private val captionInnerPadding = PaddingValues(start = 12.dp)
private val captionMinHeight = 48.dp
private val captionBackgroundColor = Color.Black.copy(alpha = 0.5f)

private val captionTextFieldPadding = PaddingValues(vertical = 12.dp)

@Composable
internal fun Caption(
    onCaptionChange: (String) -> Unit,
    caption: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(brush = captionBrush)
            .navigationBarsPadding()
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        val keyboardTransitionState = rememberKeyboardTransitionState()

        val updateTransition = updateTransition(keyboardTransitionState.isOpening)

        LaunchedEffect(keyboardTransitionState.isOpening) {
            if (keyboardTransitionState.isOpening.not()) {
                focusManager.clearFocus(force = true)
            }
        }

        val padding by updateTransition.animateDp(
            targetValueByState = { imeVisible ->
                if (imeVisible) 0.dp else captionDefaultOuterPadding
            }
        )

        val corners by updateTransition.animateFloat(
            targetValueByState = {
                if (it) 0f else captionDefaultCornerRadius
            }
        )

        Row(
            modifier = Modifier
                .imePadding()
                .heightIn(min = captionMinHeight)
                .padding(padding)
                .clip(RoundedCornerShape(corners))
                .background(captionBackgroundColor)
                .padding(captionInnerPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                modifier = Modifier
                    .padding(captionTextFieldPadding)
                    .animateContentSize()
                    .weight(1f),
                value = caption,
                onValueChange = onCaptionChange,
                decorationBox = { innerTextField ->
                    if (caption.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.screen_media_notes__toolbar__hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    innerTextField()
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                cursorBrush = SolidColor(Color.White)
            )

            AnimatedVisibility(
                visible = keyboardTransitionState.isOpening
            ) {
                MediaIconButton(
                    painter = rememberVectorPainter(Icons.Rounded.Check),
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                    },
                    colors = MediaIconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                )
            }
        }
    }
}
