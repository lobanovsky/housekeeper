package ru.housekeeper.repository.registry

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.registry.Registry

@Repository
interface RegistryRepository : CrudRepository<Registry, Long> {
}