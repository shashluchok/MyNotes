package com.shashluchok.medianotes.presentation.components.dialog

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shashluchok.medianotes.presentation.components.AnimatedVisibilityOnDisplay
import com.shashluchok.medianotes.presentation.data.AlertDialogData

@Composable
internal fun MediaAlertDialog(
    alertDialogData: AlertDialogData?,
    modifier: Modifier = Modifier
) {
    alertDialogData?.let {
        AnimatedVisibilityOnDisplay(
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutHorizontally { it }
        ) {
            AlertDialog(
                modifier = modifier,
                title = {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = it.message?.let {
                    {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onDismissRequest = it.onDismiss,
                confirmButton = {
                    TextButton(
                        onClick = it.confirmButton.onClick,
                        enabled = it.confirmButton.enabled
                    ) {
                        Text(
                            text = it.confirmButton.title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = it.dismissButton.onClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = it.dismissButton.title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    }
}
