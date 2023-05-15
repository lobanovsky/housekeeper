package ru.housekeeper.rest

import com.fasterxml.jackson.annotation.JsonFormat
import org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import ru.housekeeper.exception.EntityNotFoundException
import ru.housekeeper.utils.logger
import java.io.IOException
import java.time.LocalDateTime

@ControllerAdvice
class ErrorControllerAdvice {

    @ExceptionHandler(IOException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun brokenPipeHandler(e: IOException) = if (getRootCauseMessage(e).contains("Broken pipe", true)) null else e.message

    @ExceptionHandler(
        NoSuchElementException::class,
        EntityNotFoundException::class,
    )
    fun notFoundException(e: Exception): ResponseEntity<ErrorResponse> {
        logger().error(e.message, e)
        val httpStatus = HttpStatus.NOT_FOUND
        return ResponseEntity
            .status(httpStatus)
            .body(
                ErrorResponse(
                    status = httpStatus.value(),
                    error = httpStatus.reasonPhrase,
                    message = e.message
                )
            )
    }


    @ExceptionHandler(Exception::class)
    fun exception(e: Exception): ResponseEntity<ErrorResponse> {
        logger().error(e.message, e)
        val httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        return ResponseEntity
            .status(httpStatus)
            .body(
                ErrorResponse(
                    status = httpStatus.value(),
                    error = httpStatus.reasonPhrase,
                    message = getRootCauseMessage(e)
                )
            )
    }

    data class ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String?,
    )
}

