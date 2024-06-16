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

    @PostMapping(path = ["/manual-account"])
    @Operation(summary = "Check and create new registry for manual account")
    fun getManualRegistry(): ResponseEntity<ByteArray> {
        val registry = registryService.makeByManualAccount(specialAccount = true)
        logger().info("Registry size: ${registry.size}")
        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_manual_registry.txt"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(registry.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251")))
    }

    @PostMapping(path = ["/special-account"])
    @Operation(summary = "Check and create new registry for special account")
    fun getSpecialRegistry(
        @RequestParam(value = "useInactiveAccount", required = false, defaultValue = "false") useInactiveAccount: Boolean,
    ): ResponseEntity<ByteArray> {
        val registry = registryService.make(specialAccount = true, useInactiveAccount)
        logger().info("Registry size: ${registry.size}")
        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_special_registry.txt"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(registry.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251")))
    }

    @PostMapping(path = ["/account"])
    @Operation(summary = "Check and create new registry for account")
    fun getRegistry(
        @RequestParam(value = "useInactiveAccount", required = false, defaultValue = "false") useInactiveAccount: Boolean,
    ): ResponseEntity<ByteArray> {
        val registry = registryService.make(specialAccount = false, useInactiveAccount)
        logger().info("Registry size: ${registry.size}")
        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_registry.txt"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(registry.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251")))
    }

}