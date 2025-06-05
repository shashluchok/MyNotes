package com.shashluchok.medianotes.presentation

import android.os.Build

internal val CAMERA_AND_IMAGES: List<String>
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.CAMERA
            )
        } else {
            listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
        }
    }

internal const val RECORD_AUDIO = android.Manifest.permission.RECORD_AUDIO
