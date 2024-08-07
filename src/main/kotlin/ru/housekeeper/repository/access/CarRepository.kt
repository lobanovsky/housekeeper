package ru.housekeeper.repository.access

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.Car

@Repository
interface CarRepository : CrudRepository<Car, Long>