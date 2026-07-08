package ru.housekeeper

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.entity.OwnerEntity
import ru.housekeeper.model.entity.Room
import ru.housekeeper.model.entity.access.AccessEntity
import ru.housekeeper.model.filter.RoomFilter
import ru.housekeeper.repository.access.AccessRepository
import ru.housekeeper.repository.owner.OwnerRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.service.RoomService
import java.math.BigDecimal
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class RoomOwnerContactsTest {

    @InjectMockKs
    private lateinit var roomService: RoomService

    @MockK
    private lateinit var roomRepository: RoomRepository

    @MockK
    private lateinit var ownerRepository: OwnerRepository

    @MockK
    private lateinit var accessRepository: AccessRepository

    @Test
    fun findOwnerContactsReturnsRoomOwnerRowsWithActiveAccessPhones() {
        val filter = RoomFilter()
        val room = room(id = 1, number = "101", type = RoomTypeEnum.FLAT, owners = mutableSetOf(10, 11))
        val garage = room(id = 2, number = "55", type = RoomTypeEnum.GARAGE, owners = mutableSetOf(10))
        val office = room(id = 3, number = "1", type = RoomTypeEnum.OFFICE, owners = mutableSetOf(12))

        every { roomRepository.findAllWithFilter(0, 5000, filter) } returns PageImpl(listOf(office, garage, room))
        every { ownerRepository.findByIds(setOf(10, 11)) } returns listOf(
            OwnerEntity(id = 10, fullName = "Owner One"),
            OwnerEntity(id = 11, fullName = "Owner Two"),
        )
        every { accessRepository.findByOwnerIds(setOf(10, 11), true) } returns listOf(
            AccessEntity(id = 100, ownerId = 10, phoneNumber = "79990000002", phoneLabel = "Resident Two"),
            AccessEntity(id = 101, ownerId = 10, phoneNumber = "79990000001"),
            AccessEntity(id = 102, ownerId = 10, phoneNumber = "79990000001", phoneLabel = "Resident One"),
        )

        val result = roomService.findOwnerContacts(filter)

        assertEquals(3, result.size)
        assertEquals(listOf("101", "101", "55"), result.map { it.roomNumber })
        assertEquals(listOf(10L, 11L, 10L), result.map { it.ownerId })
        assertEquals(listOf("79990000001", "79990000002"), result[0].phones.map { it.phoneNumber })
        assertEquals(listOf("Resident One", "Resident Two"), result[0].phones.map { it.fullName })
        assertEquals(emptyList(), result[1].phones)
    }

    @Test
    fun findOwnerContactsKeepsExplicitRoomTypeFilter() {
        val filter = RoomFilter(type = RoomTypeEnum.OFFICE)
        val office = room(id = 3, number = "1", type = RoomTypeEnum.OFFICE, owners = mutableSetOf(12))

        every { roomRepository.findAllWithFilter(0, 5000, filter) } returns PageImpl(listOf(office))
        every { ownerRepository.findByIds(setOf(12)) } returns listOf(OwnerEntity(id = 12, fullName = "Office Owner"))
        every { accessRepository.findByOwnerIds(setOf(12), false) } returns listOf(
            AccessEntity(id = 103, ownerId = 12, phoneNumber = "79990000003", phoneLabel = "Office User", active = false),
        )

        val result = roomService.findOwnerContacts(filter, activeAccess = false)

        assertEquals(1, result.size)
        assertEquals(RoomTypeEnum.OFFICE, result.first().roomType)
        assertEquals("79990000003", result.first().phones.first().phoneNumber)
        assertEquals("Office User", result.first().phones.first().fullName)
    }

    private fun room(id: Long, number: String, type: RoomTypeEnum, owners: MutableSet<Long>) = Room(
        id = id,
        buildingId = 1,
        ownerName = "Owner",
        number = number,
        certificate = null,
        square = BigDecimal.TEN,
        percentage = BigDecimal.ONE,
        type = type,
        owners = owners,
    )
}
