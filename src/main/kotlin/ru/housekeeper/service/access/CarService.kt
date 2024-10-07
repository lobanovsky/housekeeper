package ru.housekeeper.service.access

import org.springframework.stereotype.Service

@Service
class CarService(
//    private val carRepository: CarRepository,
) {

//    fun createCar(
//        contactId: Long,
//        carNumber: String,
//        description: String? = null,
//        active: Boolean = true,
//    ): Car {
//        //check if car number is valid
//        if (!isValidRussianCarNumber(carNumber)) {
//            logger().error("Car number $carNumber is not valid")
//            throw IllegalArgumentException("Автомобильный номер $carNumber не является валидным")
//        }
//        //check by exist, if not then add
//        val existCar = findByNumber(carNumber)
//        if (existCar != null) {
//            logger().warn("Car with number $carNumber already exists")
//            return existCar
//        }
//        //create car
//        return carRepository.save(
//            Car(
//                createDate = LocalDateTime.now(),
//                plateNumber = carNumber,
//                description = description,
//                accessId = contactId,
//                active = active,
//            )
//        )
//    }
//
//    fun updateCars(accessId: Long, accessCars: Set<CarRequest>) {
//        val associateCarsFromReq = accessCars.associateBy { it.plateNumber }
//
//        //get all cars by accessId
//        val cars = carRepository.findByContractId(accessId, true)
//        //get all car numbers
//        val carNumbers = cars.map { it.plateNumber }
//        //get all car numbers from request
//        val carNumbersFromRequest = accessCars.map { it.plateNumber }
//        //get all car numbers that are not in the request
//        val carNumbersToDelete = carNumbers.minus(carNumbersFromRequest)
//        //get all car numbers that are in the request
//        val carNumbersToSave = carNumbersFromRequest.minus(carNumbers)
//        //delete all cars that are not in the request
//        carNumbersToDelete.forEach {
//            findByNumber(it)?.let { car ->
//                car.active = false
//                carRepository.save(car)
//            }
//        }
//        //save all cars that are in the request
//        carNumbersToSave.forEach {
//            createCar(accessId, it, associateCarsFromReq[it]?.description)
//        }
//        //update descriptions if change
//        cars.forEach {
//            associateCarsFromReq[it.plateNumber]?.let { carFromRequest ->
//                if (it.description != carFromRequest.description) {
//                    it.description = carFromRequest.description
//                    carRepository.save(it)
//                }
//            }
//        }
//    }
//
//    fun createCars(contactId: Long, cars: Set<CarRequest>?, active: Boolean) = cars?.forEach { createCar(contactId, it.plateNumber, it.description, active) }
//
//    fun findByContactId(contactId: Long, active: Boolean = true): List<Car> = carRepository.findByContractId(contactId, active)
//
//    fun findByNumberLike(carNumber: String, active: Boolean): List<Car> = carRepository.findByNumberLike(carNumber, active)
//
//    fun findByNumber(carNumber: String, active: Boolean = true): Car? = carRepository.findByNumber(carNumber, active)
//
//    fun deactivateCar(id: Long) = carRepository.deactivateById(id)
//
//    fun deactivateCars(ids: List<Long>) = carRepository.deactivateByIds(ids)

}