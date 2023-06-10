package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.FileTypeEnum
import ru.housekeeper.model.dto.FileVO
import ru.housekeeper.model.filter.FileFilter
import ru.housekeeper.service.*
import ru.housekeeper.service.gate.LogEntryService
import ru.housekeeper.utils.toFileVO

@CrossOrigin
@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService,
    private val paymentService: PaymentService,
    private val logEntryService: LogEntryService,
) {

    @GetMapping("/types")
    @Operation(summary = "Get all types of files")
    fun getFileTypes(): List<FileTypeResponse> = FileTypeEnum.values().map { FileTypeResponse(it.name, it.description) }

    data class FileTypeResponse(
        val name: String,
        val description: String,
    )

    @Operation
    @PostMapping
    fun getAllFiles(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        filter: FileFilter
    ): Page<FileVO> = fileService.findWithFilter(pageNum, pageSize, filter).toFileVO(pageNum, pageSize)

    @Operation(summary = "Remove payments by file id")
    @DeleteMapping(value = ["/payments/{fileId}"])
    fun removePaymentsByFileId(
        @PathVariable fileId: Long
    ): Int = paymentService.removePaymentsByCheckSum(fileId = fileId, checksum = fileService.findById(fileId).checksum)

    @Operation(summary = "Remove log entries by file id")
    @DeleteMapping(value = ["/log-entries/{fileId}"])
    fun removeLogEntriesByFileId(
        @PathVariable fileId: Long
    ): Int = logEntryService.removeByChecksum(fileId = fileId, checksum = fileService.findById(fileId).checksum)

}