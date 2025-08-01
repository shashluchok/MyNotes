package com.shashluchok.medianotes.presentation.components.dialog

import androidx.annotation.RawRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.components.AnimatedVisibilityOnDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoadingAnimationDialog(
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibilityOnDisplay(
            enter = fadeIn(tween()) + slideInVertically(tween()) { it },
            exit = fadeOut() + slideOutVertically(tween()) { it }
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loader))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            LottieAnimation(
                composition = composition,
                progress = { progress }
            )
        }
    }
}
