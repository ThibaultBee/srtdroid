package io.github.thibaultbee.srtdroid.example.tests

interface Test {
    val name: String
    suspend fun run(ip: String, port: Int)
}
