package com.shashluchok.medianotes.presentation.data

internal data class AlertDialogData(
    val confirmButton: ActionButton,
    val dismissButton: ActionButton,
    val title: String,
    val message: String? = null,
    val onDismiss: () -> Unit
)
