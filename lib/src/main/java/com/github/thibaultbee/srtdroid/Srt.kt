package com.github.thibaultbee.srtdroid

class Srt {
    init {
        System.loadLibrary("jnisrt")
    }

    external fun startUp(): Int
    external fun cleanUp(): Int
    external fun getVersion(): Int
    external fun setLogLevel(level: Int)
}