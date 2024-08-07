package ru.housekeeper.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Building

@Repository
interface BuildingRepository : CrudRepository<Building, Long>