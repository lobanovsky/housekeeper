package ru.housekeeper

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.housekeeper.model.dto.access.CarRequest
import ru.housekeeper.model.entity.access.Car
import ru.housekeeper.repository.access.AccessRepository
import ru.housekeeper.service.AreaService
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
import ru.housekeeper.service.access.AccessService

@ExtendWith(MockKExtension::class)
class CarTest {

    @InjectMockKs
    private lateinit var accessService: AccessService

    @MockK
    private lateinit var accessRepository: AccessRepository

    @MockK
    private lateinit var ownerService: OwnerService

    @MockK
    private lateinit var areaService: AreaService

    @MockK
    private lateinit var roomService: RoomService


    @Test
    fun enableCar() {
        val existCar = Car("х111хх77", "Audi", false)
        val newCar = CarRequest("х111хх77", "Audi")
        val carMerge = accessService.carMerge(listOf(newCar), listOf(existCar))
        assert(carMerge[0].description == "Audi" && carMerge[0].active == true)
    }
    @Test
    fun enableAndUpdateCar() {
        val existCar = Car("х111хх77", "Audi", false)
        val newCar = CarRequest("х111хх77", "Audi A5")
        val carMerge = accessService.carMerge(listOf(newCar), listOf(existCar))
        assert(carMerge[0].description == "Audi A5" && carMerge[0].active == true)
    }

    @Test
    fun updateCar() {
        val existCar1 = Car("х111хх77", "Audi")
        val existCar2 = Car("х222хх77", "BMW")
        val newCar1 = CarRequest("х111хх77", "Audi A5")
        val newCar2 = CarRequest("х222хх77", "BMW M5")
        val carMerge = accessService.carMerge(listOf(newCar1, newCar2), listOf(existCar1, existCar2))
        assert(carMerge[0].description == "Audi A5")
        assert(carMerge[1].description == "BMW M5")
    }

    @Test
    fun deleteCar() {
        val existCar1 = Car("х111хх77", "Audi")
        val existCar2 = Car("х222хх77", "BMW")
        val newCar1 = CarRequest("х999хх77", "Audi")
        val newCar2 = CarRequest("х888хх77", "BMW")
        val carMerge = accessService.carMerge(listOf(newCar1, newCar2), listOf(existCar1, existCar2))
        assert(carMerge[0].plateNumber == "х999хх77" && carMerge[0].active == true)
        assert(carMerge[1].plateNumber == "х888хх77" && carMerge[1].active == true)
        assert(carMerge[2].plateNumber == "х111хх77" && carMerge[2].active == false)
        assert(carMerge[3].plateNumber == "х222хх77" && carMerge[3].active == false)
    }

    @Test
    fun addCar() {
        val existCar1 = Car("х111хх77", "Audi")
        val existCar2 = Car("х222хх77", "BMW")
        val newCar1 = CarRequest("х111хх77", "Audi A5")
        val newCar2 = CarRequest("х222хх77", "BMW M5")
        val newCar3 = CarRequest("х333хх77", "Lada")
        val newCar4 = CarRequest("х444хх77", "Suzuki")
        val carMerge = accessService.carMerge(listOf(newCar1, newCar2, newCar3, newCar4), listOf(existCar1, existCar2))
        assert(carMerge[0].plateNumber == "х111хх77" && carMerge[0].active == true)
        assert(carMerge[1].plateNumber == "х222хх77" && carMerge[1].active == true)
        assert(carMerge[2].plateNumber == "х333хх77" && carMerge[2].active == true)
        assert(carMerge[3].plateNumber == "х444хх77" && carMerge[3].active == true)
    }

}