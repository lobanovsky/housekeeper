package ru.housekeeper.service.access

import org.springframework.stereotype.Service
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
            logger().warn("Car number $carNumber is not valid")
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

    fun findByAccessInfo(accessInfo: Long, active: Boolean): List<Car> {
        return carRepository.findByAccessInfoId(accessInfo, active)
    }

    fun findByCarNumber(carNumber: String, active: Boolean): Car? {
        return carRepository.findByNumber(carNumber, active)
    }

}