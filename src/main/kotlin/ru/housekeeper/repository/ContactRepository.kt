package ru.housekeeper.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Contact

@Repository
interface ContactRepository : CrudRepository<Contact, Long> {
}