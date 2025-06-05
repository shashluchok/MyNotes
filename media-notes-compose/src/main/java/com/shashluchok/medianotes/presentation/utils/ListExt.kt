package com.shashluchok.medianotes.presentation.utils

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal fun <T, R> ImmutableList<T>.copyModified(modify: MutableList<T>.() -> List<R>) =
    this.toMutableList().modify().toPersistentList()

internal fun <T> MutableList<T>.addOrRemove(element: T) = apply {
    if (contains(element)) {
        remove(element)
    } else {
        add(element)
    }
}
