package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import ru.housekeeper.model.entity.Workspace
import ru.housekeeper.model.request.WorkspaceRequest
import ru.housekeeper.service.WorkspaceService


@Tag(name = "Workspaces")
@CrossOrigin
@RestController
@RequestMapping("/workspaces")
class WorkspaceController(
    private val workspaceService: WorkspaceService,
) {

    @GetMapping
    fun getAllWorkspaces(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(value = "name", required = false) name: String?,
    ) = workspaceService.findAll(pageNum, pageSize, name)

    @GetMapping("/{workspaceId}")
    fun getWorkspaceById(@PathVariable workspaceId: Long): Workspace? = workspaceService.findById(workspaceId)

    @PostMapping
    fun createWorkspace(@RequestBody account: WorkspaceRequest): Workspace = workspaceService.create(account)

    @PutMapping("/{workspaceId}")
    fun updateWorkspace(
        @PathVariable workspaceId: Long, @RequestBody account: WorkspaceRequest
    ): Workspace = workspaceService.update(workspaceId, account)

    @DeleteMapping("/{workspaceId}")
    fun deleteWorkspace(@PathVariable workspaceId: Long) = workspaceService.deactivate(workspaceId)

}