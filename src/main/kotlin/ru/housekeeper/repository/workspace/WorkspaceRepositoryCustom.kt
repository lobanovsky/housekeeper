package ru.housekeeper.repository.workspace

import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository
import ru.housekeeper.model.entity.Workspace

@Repository
interface WorkspaceRepositoryCustom {

    fun findBy(
        pageNum: Int,
        pageSize: Int,
        name: String?,
    ): Page<Workspace>


}
