package ru.tsn.housekeeper.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.tsn.housekeeper.model.entity.Counterparty

@Repository
interface CounterpartyRepository : CrudRepository<Counterparty, Long> {

}
