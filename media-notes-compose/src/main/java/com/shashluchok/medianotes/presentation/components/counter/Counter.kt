package com.shashluchok.medianotes.presentation.components.counter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.shashluchok.medianotes.R

@Composable
internal fun Counter(
    current: Int,
    contentColor: Color,
    modifier: Modifier = Modifier,
    total: Int? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        current.toString().forEach { digit ->
            AnimatedContent(
                targetState = digit,
                transitionSpec = {
                    if (targetState < initialState) {
                        slideInVertically { -it } togetherWith slideOutVertically { it }
                    } else {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    }
                }
            ) {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor
                )
            }
        }

        total?.let {
            Text(
                text = " " + stringResource(R.string.counter__total__title, it),
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
        }
    }
}
