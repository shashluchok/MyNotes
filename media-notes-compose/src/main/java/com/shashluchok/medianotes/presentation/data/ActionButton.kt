package com.shashluchok.medianotes.presentation.data

data class ActionButton(
    val title: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)
