package com.shashluchok.medianotes.presentation.components.notification

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.shashluchok.medianotes.R
import com.shashluchok.medianotes.presentation.data.NotificationData

internal object NotificationAnimatedIconFactory {

    @Composable
    fun getByType(type: NotificationData.IconType): Int {
        return when (type) {
            NotificationData.IconType.TEXT_COPIED -> if (isSystemInDarkTheme()) {
                R.raw.lottie_copy_dark
            } else {
                R.raw.lottie_copy
            }
        }
    }
}
