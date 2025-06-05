package com.shashluchok.medianotes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shashluchok.medianotes.container.MediaNotesAppContainer
import com.shashluchok.medianotes.di.appModule
import com.shashluchok.medianotes.presentation.screen.MediaNotesScreen
import com.shashluchok.medianotes.presentation.theme.MediaNotesTheme
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.KoinApplication

@Composable
fun MediaNotes(
    container: MediaNotesAppContainer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidLogger()
            androidContext(context)
            modules(appModule(container))
        }
    ) {
        MediaNotesTheme {
            MediaNotesScreen(
                modifier = modifier
            )
        }
    }
}
