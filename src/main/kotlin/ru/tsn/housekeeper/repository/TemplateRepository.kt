package ru.tsn.housekeeper.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.tsn.housekeeper.model.entity.Template

@Repository
interface TemplateRepository : CrudRepository<Template, Long> {

    @Query("SELECT t FROM Template t WHERE t.name = :name")
    fun findByName(@Param("name") name: String): Template?
}