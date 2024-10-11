package ru.housekeeper

import kotlin.test.Test

class LogEntryTest {

    @Test
    fun subList() {
        val list = listOf(1, 2, 3, 4, 5)
        val size = list.size
        println(size)
        val subList = list.subList(0, if (size < 10) size else 10)
        println(subList)
        assert(subList == listOf(2, 3))
    }
}