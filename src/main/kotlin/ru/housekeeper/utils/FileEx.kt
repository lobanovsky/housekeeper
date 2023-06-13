package ru.housekeeper.utils

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import ru.housekeeper.model.dto.FileType
import ru.housekeeper.model.dto.FileVO
import ru.housekeeper.model.entity.File

fun getExcelReport(fileName: String, excelMaker: () -> ByteArray) = ResponseEntity.ok()
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
    .body(excelMaker.invoke())


fun File.toFileVO(): FileVO {
    return FileVO(
        id = id,
        name = name,
        size = size,
        checksum = checksum,
        type = FileType(
            name = fileType.name,
            description = fileType.description
        ),
        createDate = createDate
    )
}

fun Page<File>.toFileVO(pageNum: Int, pageSize: Int): Page<FileVO> =
    PageableExecutionUtils.getPage(this.content.map { it.toFileVO() }, PageRequest.of(pageNum, pageSize)) { this.totalElements }
