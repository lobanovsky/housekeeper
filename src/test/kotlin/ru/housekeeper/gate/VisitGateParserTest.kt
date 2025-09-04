package ru.housekeeper.gate

import org.junit.jupiter.api.Test
import ru.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.housekeeper.enums.gate.LogEntryStatusEnum
import ru.housekeeper.model.entity.gate.Gate
import ru.housekeeper.parser.gate.LogEntryParser
import java.time.LocalDateTime
import kotlin.test.assertEquals

class VisitGateParserTest {

    @Test
    fun logEntriesParser() {
        val log = """
            2025.09.02 18:55:09 C1 opened by user(CLOUD).
            2022.12.24 13:03:24 Opened by user:office5-1(call
            R:1):+79991112233
            2022.12.24 13:03:16 Opened by user:office5-1(call
            R:1):+79991112233
            2022.12.24 12:47:01 Opened by user:93-1(call
            R:1):+79991112233
            2022.12.24 12:43:17 Opened by user:60-2(call
            R:1):+79991112233
            2022.12.24 12:42:07 Opened by user:96-2(call
            R:1):+79991112233
            2022.12.24 12:33:49 Opened by user:126-2(call
            R:1):+79991112233
            2022.12.24 12:18:15 W: Num in 140 deleted
            2022.12.24 12:14:16 Auth. failed user(call):+79991112233
            2022.12.24 12:12:48 W: User 3-33 [79807210309] added in 140
            2022.12.24 12:07:13 W: User 18-2 [79258292090] added in 240
            2022.12.24 12:06:55 Opened by user:90-3(call
            R:1):+79991112233
            2022.12.24 12:06:55 Opened by user:90-3(call
            R:1):+79991112233
            2022.12.24 12:05:56 W: User 18-1 [79160318127] added in 210
            2022.12.24 12:04:19 Auth. failed user(call):+79991112233
            2022.12.24 12:02:21 Opened by user(call
            R:1):+79991112233
            2022.12.24 11:59:58 Opened by user:113-1(call
            R:1):+79991112233
            2022.12.24 11:59:48 Auth. failed user(call):+79991112233
            2022.12.24 11:55:56 W: User 3-33 [79859829331] added in 140
            2022.12.24 11:42:03 Opened by user:90-2(call
            R:1):+79991112233
            2022.12.24 11:37:14 Opened by user:39-3(call
            R:1):+79991112233
            2022.12.24 11:36:00 Opened by user:39-1(call
            R:1):+79991112233
            2022.12.24 11:35:38 Opened by user:90-3(call
            R:1):+79991112233
            2022.12.24 11:35:35 W: User 3-1 [79859852014] added in 139
            2022.12.24 11:33:45 W: Num in 320 deleted
            2022.12.24 11:31:04 Opened by user:16-1(APP
            R:1):+79991112233
        """.trimIndent()

        val uniqueEntries = LogEntryParser().parseLogEntries(log)
        assertEquals(uniqueEntries.size, 25)
    }

    @Test
    fun parseLogEntriesByType() {
        val dateTime = LocalDateTime.now()

        val gate = Gate(
            id = 1,
            createDate = LocalDateTime.now(),
            imei = "123456789012345",
            model = "model",
            name = "name",
            phoneNumber = "1234567890",
            firmware = "firmware",
            deviceId = "38344"
        )
        //opened
        val openedLine = "Opened by user:tehnik(call R:1):+79991112233"
        val openedResult = LogEntryParser().openParser(dateTime, openedLine, gate)
        assertEquals(dateTime, openedResult.dateTime)
        assertEquals(LogEntryStatusEnum.OPENED, openedResult.status)
        assertEquals(LogEntryAccessMethodEnum.CALL, openedResult.method)
        assertEquals("tehnik", openedResult.userName)
        assertEquals("79991112233", openedResult.phoneNumber)

        //auth failed
        val authFailedLine = "Auth. failed user(call):+79991112233"
        val authFailedResult = LogEntryParser().authFailedParser(dateTime, authFailedLine, gate)
        assertEquals(dateTime, authFailedResult.dateTime)
        assertEquals(LogEntryStatusEnum.AUTH_FAILED, authFailedResult.status)
        assertEquals(LogEntryAccessMethodEnum.CALL, authFailedResult.method)
        assertEquals("79991112233", authFailedResult.phoneNumber)

        //deleted
        val deletedLine = "W: Num in 320 deleted"
        val deleteResult = LogEntryParser().numDeletedParser(dateTime, deletedLine, gate)
        assertEquals(dateTime, deleteResult.dateTime)
        assertEquals(LogEntryStatusEnum.NUM_DELETED, deleteResult.status)
        assertEquals("320", deleteResult.cell)

        //user added
        val addedLine = "W: User 3-33 [79991112233] added in 140"
        val addedResult = LogEntryParser().userAddedParser(dateTime, addedLine, gate)
        assertEquals(addedResult.dateTime, dateTime)
        assertEquals(addedResult.status, LogEntryStatusEnum.USER_ADDED)
        assertEquals(addedResult.userName, "3-33")
        assertEquals(addedResult.phoneNumber, "79991112233")
        assertEquals(addedResult.cell, "140")

    }

}