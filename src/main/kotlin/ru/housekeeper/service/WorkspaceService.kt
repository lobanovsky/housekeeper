package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.housekeeper.model.entity.Workspace
import ru.housekeeper.model.request.WorkspaceRequest
import ru.housekeeper.repository.workspace.WorkspaceRepository
import ru.housekeeper.utils.entityNotfound
import kotlin.let
import kotlin.to

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository
) {

    fun create(workspace: WorkspaceRequest): Workspace {
        workspaceRepository.findByNameAndActive(workspace.name)?.let {
            throw IllegalArgumentException("Рабочее пространство с именем ${it.name} уже существует")
        }
        return workspaceRepository.save(workspace.toEntity())
    }

    fun findAll(
        pageNum: Int,
        pageSize: Int,
        name: String?,
    ) = workspaceRepository.findBy(pageNum, pageSize, name)

    fun findById(id: Long): Workspace =
        workspaceRepository.findByIdOrNull(id) ?: entityNotfound("Рабочее пространство" to id)

    fun findActiveByIds(ids: Collection<Long>) = workspaceRepository.findByActiveAndIdIn(active = true, ids)

    fun update(id: Long, workspace: WorkspaceRequest): Workspace {
        workspaceRepository.findByNameAndActive(workspace.name)?.let {
            throw IllegalArgumentException("Рабочее пространство с именем ${it.name} уже существует")
        }
        val existingWorkspace = workspaceRepository.findByIdOrNull(id) ?: entityNotfound("Рабочее пространство" to id)
        existingWorkspace.name = workspace.name
        return workspaceRepository.save(existingWorkspace)
    }

    @Transactional
    fun deactivate(id: Long) = workspaceRepository.updateActiveById(id, false)

}