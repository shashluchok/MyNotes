package com.shashluchok.medianotes.container

interface AppInfoProvider {
    data class TopBarConfiguration(
        val title: String,
        val onDismiss: () -> Unit
    )

    val topBarConfiguration: TopBarConfiguration
}
