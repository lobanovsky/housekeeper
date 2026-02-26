package ru.housekeeper.rest.gate

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import ru.housekeeper.service.gate.GateSyncService

@CrossOrigin
@RestController
@RequestMapping("/gates")
class GateSyncController(
    private val gateSyncService: GateSyncService,
) {

    @Operation(summary = "Синхронизировать все шлагбаумы с eldes-api")
    @PostMapping("/sync")
    fun syncAll(): List<GateSyncService.GateSyncResult> = gateSyncService.syncAll()

    @Operation(summary = "Синхронизировать конкретный шлагбаум с eldes-api")
    @PostMapping("/{id}/sync")
    fun syncGate(@PathVariable id: Long): GateSyncService.GateSyncResult = gateSyncService.syncGate(id)
}
