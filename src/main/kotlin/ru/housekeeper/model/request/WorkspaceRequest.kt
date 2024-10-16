package ru.housekeeper.model.request

import ru.housekeeper.model.entity.Workspace


data class WorkspaceRequest(
    val name: String,
) {

    fun toEntity(): Workspace {
        return Workspace(
            name = this.name,
        )
    }
}