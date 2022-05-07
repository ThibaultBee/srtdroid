package io.github.thibaultbee.srtdroid.models

import io.github.thibaultbee.srtdroid.Srt
import io.github.thibaultbee.srtdroid.enums.GroupType
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SocketGroupTest {
    private lateinit var group: SocketGroup

    @Before
    fun setUp() {
        group = SocketGroup(GroupType.BROADCAST)
        assertTrue(group.isValid)
    }

    @After
    fun tearDown() {
        assertEquals(Srt.cleanUp(), 0)
    }

    @Test
    fun createGroupForEachTypes() {
        GroupType.values().forEach {
            SocketGroup(it)
        }
    }

    @Test
    fun connect() {

    }
}