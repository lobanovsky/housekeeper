package ru.housekeeper.rest.registry

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.housekeeper.service.registry.RegistryService
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.yyyyMMddHHmmssDateFormat
import java.nio.charset.Charset
import java.time.LocalDateTime

@CrossOrigin
@RestController
@RequestMapping("/registries")
class RegistryController(
    private val registryService: RegistryService,
) {

    data class RegistryFilter(val bankAccount: String)

    @PostMapping
    @Operation(summary = "Check or create new registry")
    fun getRegistry(
        @RequestBody filter: RegistryFilter,
    ): ResponseEntity<ByteArray> {
        val registry = registryService.make(filter.bankAccount)
        logger().info("Registry size: ${registry.size}")
        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_registry.txt"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(registry.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251")))
    }

}