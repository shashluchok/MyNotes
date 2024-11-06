package com.shashluchok.medianotes.presentation.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal abstract class AbsViewModel<STATE> : ViewModel() {

    protected abstract val mutableStateFlow: MutableStateFlow<STATE>
    val stateFlow: StateFlow<STATE> get() = mutableStateFlow

    var state: STATE
        get() = stateFlow.value
        protected set(value) {
            mutableStateFlow.value = value
        }
}
