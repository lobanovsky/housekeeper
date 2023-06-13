package ru.housekeeper.repository.file

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Page
import ru.housekeeper.model.entity.File
import ru.housekeeper.model.filter.FileFilter
import ru.housekeeper.repository.equalFilterBy
import ru.housekeeper.repository.likeFilterBy
import ru.housekeeper.utils.getPage

class FileRepositoryCustomImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : FileRepositoryCustom {

    override fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int, filter: FileFilter
    ): Page<File> {
        val predicates = mutableMapOf<String, String>()
        predicates["name"] = likeFilterBy("f.name", filter.name)
        predicates["type"] = equalFilterBy("f.fileType", filter.fileType)
        val conditions = predicates.values.joinToString(separator = " ")

        val sql = "SELECT f FROM File f WHERE true = true $conditions ORDER BY f.createDate"
        val sqlCount = "SELECT count(f) FROM File f WHERE true = true $conditions"

        return getPage<File>(entityManager, sql, sqlCount, pageNum, pageSize)
    }

}