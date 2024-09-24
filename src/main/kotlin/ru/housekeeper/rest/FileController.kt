package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import ru.housekeeper.enums.FileTypeEnum
import ru.housekeeper.model.dto.FileVO
import ru.housekeeper.model.filter.FileFilter
import ru.housekeeper.service.FileService
import ru.housekeeper.utils.toFileVO

@CrossOrigin
@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService,
) {

    @GetMapping("/types")
    @Operation(summary = "Get all types of files")
    fun getFileTypes(): List<FileTypeResponse> =
        FileTypeEnum.entries.filterIndexed { index, _ -> index == 0 || index == 6 }
            .map { FileTypeResponse(it.name, it.description) }

    data class FileTypeResponse(
        val name: String,
        val description: String,
    )

    @Operation
    @PostMapping
    fun getAllFiles(
        @RequestParam(value = "pageNum", required = false, defaultValue = "0") pageNum: Int,
        @RequestParam(value = "pageSize", required = false, defaultValue = "10") pageSize: Int,
        @RequestBody filter: FileFilter
    ): Page<FileVO> = fileService.findWithFilter(pageNum, pageSize, filter).toFileVO(pageNum, pageSize)

    @Operation(summary = "Remove data (payments, log-entries etc.) by the file ids")
    @DeleteMapping(value = ["{fileIds}"])
    fun remove(
        @PathVariable fileIds: List<Long>
    ): Int = fileService.deleteByIds(fileIds)

}