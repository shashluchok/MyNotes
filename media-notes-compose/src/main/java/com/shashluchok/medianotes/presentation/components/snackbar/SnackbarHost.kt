package com.shashluchok.medianotes.presentation.components.snackbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val snackbarPadding = PaddingValues(bottom = 50.dp)

internal data class SnackbarData(
    val title: String,
    val actionTitle: String? = null,
    val action: (() -> Unit)? = null,
    val onDismiss: () -> Unit = {}
)

@Composable
internal fun SnackbarHost(
    snackBarHostState: SnackbarHostState,
    snackbarData: SnackbarData?,
    modifier: Modifier = Modifier
) {
    if (snackbarData != null) {
        val title = snackbarData.title
        val actionTitle = snackbarData.actionTitle
        LaunchedEffect(snackbarData) {
            val result = snackBarHostState.showSnackbar(
                message = title,
                actionLabel = actionTitle,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                snackbarData.action?.invoke()
            }
            snackbarData.onDismiss()
        }
    }

    SnackbarHost(
        modifier = modifier.padding(snackbarPadding),
        hostState = snackBarHostState
    ) {
        Snackbar(it)
    }
}
