package com.shashluchok.medianotes.presentation.components.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.shashluchok.medianotes.presentation.data.NotificationData
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val notificationDuration = 2.seconds

private const val notificationVisibilityAnimationDuration = 300

private val notificationShape = RoundedCornerShape(12.dp)
private val notificationContentPadding = PaddingValues(12.dp)
private val notificationContentHorizontalArrangement = Arrangement.spacedBy(6.dp)

private val notificationAnimationIconSize = 24.dp
private const val notificationAnimationIconScale = 2f

@Composable
internal fun Notification(
    notificationData: NotificationData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    notificationData?.let {
        var isVisible by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(notificationData) {
            isVisible = true
            delay(notificationDuration)
            isVisible = false
            delay(notificationVisibilityAnimationDuration.toLong())
            onDismiss()
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                tween(durationMillis = notificationVisibilityAnimationDuration)
            ) + slideInVertically(
                tween(durationMillis = notificationVisibilityAnimationDuration)
            ) { -it },
            exit = fadeOut(
                tween(durationMillis = notificationVisibilityAnimationDuration)
            ) + slideOutVertically(
                tween(durationMillis = notificationVisibilityAnimationDuration)
            ) { -it }
        ) {
            Box(
                modifier = modifier
                    .clip(notificationShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(
                        NotificationAnimatedIconFactory.getByType(it.iconType)
                    )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(notificationContentPadding),
                    horizontalArrangement = notificationContentHorizontalArrangement,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        modifier = Modifier
                            .size(notificationAnimationIconSize)
                            .scale(notificationAnimationIconScale),
                        composition = composition,
                        progress = { progress },
                        contentScale = ContentScale.FillBounds,
                        clipTextToBoundingBox = true

                    )
                    Text(
                        text = stringResource(it.message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
