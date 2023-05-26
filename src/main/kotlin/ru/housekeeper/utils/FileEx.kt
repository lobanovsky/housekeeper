package ru.housekeeper.utils

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

fun getExcelReport(fileName: String, excelMaker: () -> ByteArray) = ResponseEntity.ok()
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
    .body(excelMaker.invoke())
