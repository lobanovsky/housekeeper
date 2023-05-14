package ru.tsn.housekeeper.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.tsn.housekeeper.model.entity.File

@Repository
interface FileRepository : CrudRepository<File, Long> {

    @Query("select f from File f where lower(f.name) = :name")
    fun findByName(@Param("name") name: String): File?

    @Query("select f from File f where lower(f.checksum) = :checksum")
    fun findByCheckSum(@Param("checksum") checksum: String): File?

    @Modifying
    @Query("delete from File f where f.name = :filename")
    fun removeByFilename(@Param("filename") filename: String)
}