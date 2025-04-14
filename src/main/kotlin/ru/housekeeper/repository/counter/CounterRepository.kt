package ru.housekeeper.repository.counter

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.counter.Counter

@Repository
interface CounterRepository : CrudRepository<Counter, Long> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Counter c WHERE c.number = :number AND c.roomId = :roomId")
    fun existsByNumberAndRoomId(number: String, roomId: Long): Boolean

}