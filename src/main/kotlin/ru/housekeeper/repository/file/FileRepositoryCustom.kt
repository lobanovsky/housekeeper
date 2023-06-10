package ru.housekeeper.repository.file

import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.File
import ru.housekeeper.model.filter.FileFilter

@Repository
interface FileRepositoryCustom {

    fun findAllWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: FileFilter
    ): Page<File>

}