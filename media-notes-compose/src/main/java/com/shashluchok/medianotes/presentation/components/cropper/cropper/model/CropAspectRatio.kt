package com.shashluchok.medianotes.presentation.components.cropper.cropper.model

internal enum class CropAspectRatio(
    val value: Float
) {
    RATIO_4_3(4f / 3f),
    RATIO_3_4(3f / 4f),
    RATIO_16_9(16f / 9f),
    RATIO_9_16(9f / 16f),
    RATIO_1_1(1f)
}
