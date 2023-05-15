package ru.housekeeper.repository.counter

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.counter.Counter

@Repository
interface CounterRepository : CrudRepository<Counter, Long>