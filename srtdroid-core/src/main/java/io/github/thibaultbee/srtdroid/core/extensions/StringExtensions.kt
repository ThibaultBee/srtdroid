package io.github.thibaultbee.srtdroid.core.extensions

fun String.replaceBetween(
    prefix: String,
    suffix: String,
    value: String,
): String {
    val startIndex = indexOf(prefix)
    if (startIndex < 0) return this
    val endIndex = indexOf(suffix, startIndex + prefix.length)

    return if (endIndex < 0) {
        substring(0, startIndex + prefix.length) + value
    } else {
        substring(
            0,
            startIndex + prefix.length
        ) + value + substring(endIndex)
    }
}

fun String.substringBetween(prefix: String, suffix: String): String {
    val startIndex = indexOf(prefix)
    if (startIndex < 0) return ""
    val endIndex = indexOf(suffix, startIndex + prefix.length)
    return if (endIndex < 0) {
        substring(startIndex + prefix.length)
    } else {
        substring(startIndex + prefix.length, endIndex)
    }
}
