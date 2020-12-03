package com.github.thibaultbee.srtdroid.utils

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileWriter
import java.util.*

class Utils {
    companion object {
        fun createTestFile(
            name: String? = UUID.randomUUID().toString(),
            message: String? = "Hello ! Did someone receive this message?"
        ): File {
            val file = File(
                InstrumentationRegistry.getInstrumentation().context.externalCacheDir,
                name
            )
            val fw = FileWriter(file)
            fw.write(message)
            fw.close()
            return file
        }
    }
}