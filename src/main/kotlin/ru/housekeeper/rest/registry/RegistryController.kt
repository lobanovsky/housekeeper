package ru.housekeeper.rest.registry

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.registry.RegistryService

@CrossOrigin
@RestController
@RequestMapping("/registries")
class RegistryController(
    private val registryService: RegistryService,
) {

    data class RegistryFilter(val bankAccount: String)

    @GetMapping
    @Operation(summary = "Check or create new registry")
    fun getRegistry(
//        @RequestBody filter: RegistryFilter,
    ): Int {
        return 42
//        val registry = registryService.make(filter.bankAccount)
//        logger().info("Registry size: ${registry.size}")
//        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_registry.txt"
//        return registry.size
//        return ResponseEntity.ok()
//            .contentType(MediaType.APPLICATION_OCTET_STREAM)
//            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
//            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
//            .body(registry.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251")))
    }

}