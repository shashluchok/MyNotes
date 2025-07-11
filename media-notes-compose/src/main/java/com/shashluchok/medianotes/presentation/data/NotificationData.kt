package com.shashluchok.medianotes.presentation.data

import androidx.annotation.StringRes

internal data class NotificationData(
    @StringRes val message: Int,
    val iconType: IconType
) {
    enum class IconType {
        TEXT_COPIED
    }
}
