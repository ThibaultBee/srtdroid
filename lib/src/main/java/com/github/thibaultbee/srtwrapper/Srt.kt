package com.github.thibaultbee.srtwrapper

class Srt {
    private external fun nativeStartUp() : Int
    private external fun nativeCleanUp() : Int
    private external fun nativeSetLogLevel(level: Int)

    init {
        System.loadLibrary("crypto")
        System.loadLibrary("ssl")
        System.loadLibrary("srt")
        System.loadLibrary("jnisrt")
    }

    fun startUp() : Int = nativeStartUp()
    fun cleanUp() : Int = nativeCleanUp()
    fun setLogLevel(level: Int) = nativeSetLogLevel(level)
}