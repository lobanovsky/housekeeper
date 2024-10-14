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

    /**
     * Ручное создание реестров согласно присланным платёжкам от Мос.ру
     * Мос.ру прислал Excel-и, по которым нужно понять, как распределить сумму:
     * Предыдущую сумму по каждому л/с минус присланную сумму по каждому л/с
     * В итоге, разница по всем л/с должна совпасть с платёжным поручением
     *
     * Все платёжные поручения приходят на почту
     *
     * 1. 2024-04-нежилые (24 985,05 ₽)
     * 2. 2023-10-жилые (53 471,48 ₽) /нежилые (8 195,41 ₽)
     *
     * Смотреть файл: ФКР-жилые
     * 1. Закладка: мм-пп (8 195,41) и (24 985,05)
     * 2. кв-original (53 471,49)
     */
    @PostMapping(path = ["/custom-account"])
    @Operation(summary = "Check and create new registry for manual account")
    fun getCustomRegistry(
        @RequestParam(value = "sum", required = true, defaultValue = "24985,05") sum: String,
    ): ResponseEntity<ByteArray> {
        val registry = registryService.makeCustom(sum)
        logger().info("Registry size: ${registry.size}")
        val fileName = "${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}_custom_registry.txt"
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(registry.joinToString(separator = "\r\n").toByteArray(Charset.forName("WINDOWS-1251")))
    }


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