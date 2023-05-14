package ru.tsn.housekeeper.service

import org.apache.commons.codec.binary.Hex.encodeHex
import org.apache.commons.codec.digest.DigestUtils.updateDigest
import org.apache.commons.io.FilenameUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tsn.housekeeper.exception.FileException
import ru.tsn.housekeeper.model.entity.File
import ru.tsn.housekeeper.repository.FileRepository
import ru.tsn.housekeeper.utils.entityNotfound
import ru.tsn.housekeeper.utils.logger
import java.io.InputStream
import java.security.MessageDigest

@Service
class FileService(
    val fileRepository: FileRepository
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

    fun saveFileInfo(name: String, size: Long, checksum: String): File {
        logger().info("Try save file info: name: $name size: $size checksum: $checksum")
        return fileRepository.save(
            File(
                name = name,
                size = size,
                checksum = checksum,
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

    fun deleteByid(id: Long) = fileRepository.deleteById(id)

}