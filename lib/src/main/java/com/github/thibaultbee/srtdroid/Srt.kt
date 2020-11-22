package com.github.thibaultbee.srtdroid

class Srt {
    init {
        System.loadLibrary("jnisrt")
    }

    external fun startUp(): Int
    external fun cleanUp(): Int
    private external fun nativeGetVersion(): Int
    val version: Int
        get() = nativeGetVersion()

    external fun setLogLevel(level: Int)
}