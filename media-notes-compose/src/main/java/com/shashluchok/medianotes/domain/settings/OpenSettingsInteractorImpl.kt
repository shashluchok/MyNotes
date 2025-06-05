package com.shashluchok.medianotes.domain.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal class OpenSettingsInteractorImpl : OpenSettingsInteractor {
    override operator fun invoke(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts(URI_SCHEME, context.packageName, null)
        )
        context.startActivity(intent)
    }

    companion object {
        private const val URI_SCHEME = "package"
    }
}
