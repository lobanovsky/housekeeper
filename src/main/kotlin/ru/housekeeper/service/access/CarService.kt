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
        accessId: Long,
        description: String? = null,
        active: Boolean = true,
    ): Car {
        //check if car number is valid
        if (!isValidRussianCarNumber(carNumber)) {
            logger().error("Car number $carNumber is not valid")
            throw IllegalArgumentException("Автомобильный номер $carNumber не является валидным")
        }
        //check by exist, if not then add
        val car = findByNumber(carNumber)
        if (car != null) {
            logger().warn("Car with number $carNumber already exists")
            return car
        }
        //create car
        return carRepository.save(
            Car(
                createDate = LocalDateTime.now(),
                plateNumber = carNumber,
                description = description,
                accessId = accessId,
                active = active,
            )
        )
    }

    fun updateCars(accessId: Long, accessCars: Set<AccessCar>) {
        val associateCarsFromReq = accessCars.associateBy { it.plateNumber }

        //get all cars by accessId
        val cars = carRepository.findByAccessId(accessId, true)
        //get all car numbers
        val carNumbers = cars.map { it.plateNumber }
        //get all car numbers from request
        val carNumbersFromRequest = accessCars.map { it.plateNumber }
        //get all car numbers that are not in the request
        val carNumbersToDelete = carNumbers.minus(carNumbersFromRequest)
        //get all car numbers that are in the request
        val carNumbersToSave = carNumbersFromRequest.minus(carNumbers)
        //delete all cars that are not in the request
        carNumbersToDelete.forEach {
            findByNumber(it)?.let { car ->
                car.active = false
                carRepository.save(car)
            }
        }
        //save all cars that are in the request
        carNumbersToSave.forEach {
            createCar(it, accessId, associateCarsFromReq[it]?.description)
        }
        //update descriptions if change
        cars.forEach {
            associateCarsFromReq[it.plateNumber]?.let { carFromRequest ->
                if (it.description != carFromRequest.description) {
                    it.description = carFromRequest.description
                    carRepository.save(it)
                }
            }
        }
    }

    fun addCars(accessId: Long, cars: Set<AccessCar>, active: Boolean) = cars.forEach { createCar(it.plateNumber, accessId, it.description, active) }

    fun findByAccessId(access: Long, active: Boolean = true): List<Car> = carRepository.findByAccessId(access, active)

    fun findByNumberLike(carNumber: String, active: Boolean): List<Car> = carRepository.findByNumberLike(carNumber, active)

    fun findByNumber(carNumber: String, active: Boolean = true): Car? = carRepository.findByNumber(carNumber, active)

    fun deactivateCar(id: Long) = carRepository.deactivateById(id)

}