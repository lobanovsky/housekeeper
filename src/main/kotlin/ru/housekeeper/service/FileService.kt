package ru.housekeeper.service

import org.apache.commons.codec.binary.Hex.encodeHex
import org.apache.commons.codec.digest.DigestUtils.updateDigest
import org.apache.commons.io.FilenameUtils
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.FileTypeEnum
import ru.housekeeper.exception.FileException
import ru.housekeeper.model.entity.File
import ru.housekeeper.model.filter.FileFilter
import ru.housekeeper.repository.file.FileRepository
import ru.housekeeper.service.gate.LogEntryService
import ru.housekeeper.utils.entityNotfound
import ru.housekeeper.utils.logger
import java.io.InputStream
import java.security.MessageDigest

@Service
class FileService(
    private val fileRepository: FileRepository,
    private val paymentService: PaymentService,
    private val logEntryService: LogEntryService,
) {

    fun isExtensionEqual(file: MultipartFile, extension: String = "xlsx") {
        logger().info("Upload file [name:${file.originalFilename}; type:${file.contentType}; size: ${file.size} bytes]")
        val originalExtension = FilenameUtils.getExtension(file.originalFilename)
        if (!originalExtension.equals(extension, true)) {
            throw FileException("Extension $originalExtension unsupported. File with name ${file.originalFilename} will not be uploaded")
        }
    }

    fun isDuplicateAndGetChecksum(file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: ""

        val foundFileByName = fileRepository.findByName(originalFilename)
        if (foundFileByName != null) throw FileException("File with that name [$originalFilename] has already been uploaded")

        val checkSum = getCheckSumFromFile(file.inputStream)
        val foundFileByChecksum = fileRepository.findByCheckSum(checkSum)
        if (foundFileByChecksum != null) throw FileException("File [$originalFilename] with that checksum [$checkSum] has already been uploaded")

        return checkSum
    }

    fun saveFileInfo(name: String, size: Long, checksum: String, fileType: FileTypeEnum): File {
        logger().info("Try to save file [${fileType.description}]: name: $name size: $size checksum: $checksum")
        return fileRepository.save(
            File(
                name = name,
                size = size,
                checksum = checksum,
                fileType = fileType
            )
        )
    }

    private fun getCheckSumFromFile(fis: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val byteArray = updateDigest(digest, fis).digest()
        fis.close()
        val hexCode = encodeHex(byteArray, true)
        return String(hexCode)
    }


    fun findById(id: Long): File = fileRepository.findByIdOrNull(id) ?: entityNotfound("File" to id)

    @Transactional
    fun deleteByIds(ids: List<Long>): Int {
        var count = 0
        ids.forEach {
            val existFile = findById(it)
            fileRepository.deleteById(it)
            count += when (existFile.fileType) {
                FileTypeEnum.PAYMENTS -> paymentService.removePaymentsByCheckSum(checksum = existFile.checksum)
                FileTypeEnum.LOG_ENTRY -> logEntryService.removeByChecksum(checksum = existFile.checksum)
                else -> {
                    logger().warn("Removing data for ${existFile.fileType} not implemented yet")
                    0
                }
            }
        }
        return count
    }

    fun findWithFilter(
        pageNum: Int,
        pageSize: Int,
        filter: FileFilter
    ): Page<File> = fileRepository.findAllWithFilter(pageNum, pageSize, filter)

}