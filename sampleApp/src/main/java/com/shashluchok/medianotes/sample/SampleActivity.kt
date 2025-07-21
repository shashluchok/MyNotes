package com.shashluchok.medianotes.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.shashluchok.medianotes.MediaNotes
import com.shashluchok.medianotes.container.AppInfoProvider
import com.shashluchok.medianotes.container.MediaNotesAppContainer
import com.shashluchok.medianotes.sample.container.MediaNotesRepositoryImpl
import com.shashluchok.medianotes.sample.domain.db.MediaNotesDatabase

class SampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val dataBase = Room.databaseBuilder(
            context = this,
            MediaNotesDatabase::class.java,
            DB_NAME
        ).build()

        val repository = MediaNotesRepositoryImpl(
            mediaNotesDatabase = dataBase
        )

        setContent {
            MediaNotes(
                container = object : MediaNotesAppContainer {
                    override val appInfoProvider = object : AppInfoProvider {
                        override val topBarConfiguration = AppInfoProvider.TopBarConfiguration(
                            title = SAMPLE_TOP_BAR_TITLE,
                            onDismiss = ::finish
                        )
                    }
                    override val mediaNotesRepository = repository
                }
            )
        }
    }

    companion object {
        private const val DB_NAME = "MediaNotesDatabase"
        private const val SAMPLE_TOP_BAR_TITLE = "Заметки"
    }
}
