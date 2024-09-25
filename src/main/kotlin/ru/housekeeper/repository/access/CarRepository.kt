package ru.housekeeper.repository.access

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.access.Car

@Repository
interface CarRepository : CrudRepository<Car, Long> {

    @Query("SELECT c FROM Car c WHERE c.number LIKE %:number% AND c.active = :active")
    fun findByNumber(
        @Param("number") number: String,
        @Param("active") active: Boolean = true,
    ): Car?

    @Query("SELECT c FROM Car c WHERE c.accessInfoId = :accessInfoId AND c.active = :active")
    fun findByAccessInfoId(accessInfoId: Long, active: Boolean = true): List<Car>

    //Deactivate car by id
    @Modifying
    @Query("UPDATE Car c SET c.active = false WHERE c.id = :id")
    fun deactivateById(id: Long)

}
