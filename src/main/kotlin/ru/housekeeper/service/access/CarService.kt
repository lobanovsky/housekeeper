package ru.housekeeper.service.access

import org.springframework.stereotype.Service
import ru.housekeeper.model.dto.access.AccessCar
import ru.housekeeper.model.entity.access.Car
import ru.housekeeper.repository.access.CarRepository
import ru.housekeeper.utils.isValidRussianCarNumber
import ru.housekeeper.utils.logger
import java.time.LocalDateTime

@Service
class CarService(
    private val carRepository: CarRepository,
) {

    fun createCar(
        carNumber: String,
        accessInfoId: Long,
        description: String? = null
    ): Car {
        //check if car number is valid
        if (!isValidRussianCarNumber(carNumber)) {
            logger().error("Car number $carNumber is not valid")
            throw IllegalArgumentException("Автомобильный номер $carNumber не является валидным")
        }
        //check by exist, if not then add
        val car = carRepository.findByNumber(carNumber)
        if (car != null) {
            logger().warn("Car with number $carNumber already exists")
            return car
        }
        //create car
        return carRepository.save(
            Car(
                createDate = LocalDateTime.now(),
                number = carNumber,
                description = description,
                accessInfoId = accessInfoId
            )
        )
    }

    fun updateCars(accessInfoId: Long, accessCars: Set<AccessCar>) {
        val associateByPlate = accessCars.associateBy { it.plateNumber }

        //get all cars by accessInfoId
        val cars = carRepository.findByAccessInfoId(accessInfoId, true)
        //get all car numbers
        val carNumbers = cars.map { it.number }
        //get all car numbers from request
        val carNumbersFromRequest = accessCars.map { it.plateNumber }
        //get all car numbers that are not in the request
        val carNumbersToDelete = carNumbers.minus(carNumbersFromRequest)
        //get all car numbers that are in the request
        val carNumbersToSave = carNumbersFromRequest.minus(carNumbers)
        //delete all cars that are not in the request
        carNumbersToDelete.forEach {
            carRepository.findByNumber(it)?.let { car ->
                car.active = false
                carRepository.save(car)
            }
        }
        //save all cars that are in the request
        carNumbersToSave.forEach {
            createCar(it, accessInfoId, associateByPlate[it]?.description)
        }
        //update descriptions if change
        cars.forEach {
            associateByPlate[it.number]?.let { accessCar ->
                if (it.description != accessCar.description) {
                    it.description = accessCar.description
                    carRepository.save(it)
                }
            }
        }
    }

    fun findByAccessInfo(accessInfo: Long, active: Boolean): List<Car> {
        return carRepository.findByAccessInfoId(accessInfo, active)
    }

    fun findByCarNumber(carNumber: String, active: Boolean): Car? {
        return carRepository.findByNumber(carNumber, active)
    }

    fun createCarForAccesses(accessInfoId: Long, cars: Set<AccessCar>) {
        cars.forEach {
            createCar(it.plateNumber, accessInfoId, it.description)
        }
    }

}