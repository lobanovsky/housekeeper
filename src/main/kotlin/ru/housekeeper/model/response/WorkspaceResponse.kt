package ru.housekeeper.model.response

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils
import ru.housekeeper.model.entity.Workspace
import ru.housekeeper.utils.color
import java.time.LocalDateTime

data class WorkspaceResponse(
    val id: Long,
    val createDate: LocalDateTime,
    val active: Boolean,
    val name: String,
    val color: String?,
)

fun Workspace.toResponse(): WorkspaceResponse {
    return WorkspaceResponse(
        id = this.id,
        createDate = this.createDate,
        active = this.active,
        name = this.name,
        color = this.name.color(),
    )
}

fun Page<Workspace>.toResponse(pageNum: Int, pageSize: Int): Page<WorkspaceResponse> =
    PageableExecutionUtils.getPage(this.content.map { it.toResponse() }, PageRequest.of(pageNum, pageSize)) { this.totalElements }
