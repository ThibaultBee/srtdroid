package io.github.thibaultbee.srtdroid.core.extensions

internal fun Int.toBoolean(): Boolean {
    return this != 0
}

internal fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}