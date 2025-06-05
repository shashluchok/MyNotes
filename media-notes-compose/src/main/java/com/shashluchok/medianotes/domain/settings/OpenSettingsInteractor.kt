package com.shashluchok.medianotes.domain.settings

import android.content.Context

internal interface OpenSettingsInteractor {
    operator fun invoke(context: Context)
}
