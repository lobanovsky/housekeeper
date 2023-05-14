package ru.tsn.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.tsn.housekeeper.enums.counter.CounterTypeEnum
import ru.tsn.housekeeper.service.*
import ru.tsn.housekeeper.service.counter.CounterService
import ru.tsn.housekeeper.service.gate.LogEntryService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.time.Year

@CrossOrigin
@RestController
@RequestMapping("/files")
class FileController(
    private val fileService: FileService,
    private val counterpartyService: CounterpartyService,
    private val paymentService: PaymentService,
    private val roomService: RoomService,
    private val contactService: ContactService,
    private val decisionService: DecisionService,
    private val counterService: CounterService,
    private val logEntryService: LogEntryService,
) {

    @Operation(summary = "Import eldes gate from *.log")
    @PostMapping(value = ["/eldes-gate/importer"])
    fun importEldesGate(
        @RequestPart file: MultipartFile,
    ): EldesGateInfoResponse {
        fileService.isExtensionEqual(file, "log")
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val imei = file.originalFilename?.substringAfter("IMEI")?.substringBefore("_")
            ?: throw IllegalArgumentException("File name must contain IMEI")
        val (totalSize) = logEntryService.parseAndSave(file, checkSum, imei)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return EldesGateInfoResponse(file.originalFilename, totalSize)
    }

    data class EldesGateInfoResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
    )

    @Operation(summary = "Import counter values from *.xlsx")
    @PostMapping(value = ["/counters/counter-values/importer"])
    fun importValuesOfCounters(
        @RequestPart file: MultipartFile,
    ) {
        fileService.isExtensionEqual(file)
//        val checkSum = fileService.isDuplicateAndGetChecksum(file)
//        val totalSize = counterService.upload(file, checkSum, filter)
//        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
//        return CounterInfoResponse(file.originalFilename, totalSize)
    }

    data class CounterValue(
        val values: List<Position>,
        val month: Month = LocalDate.now().month,
        val year: Year = Year.now()
    )

    data class Position(
        val counterNumberRow: Int = 1,
        val counterValueRow: Int = 2,
        val counterType: CounterTypeEnum = CounterTypeEnum.COLD_WATER
    )


    @Operation(summary = "Import water counters from *.xlsx")
    @PostMapping(value = ["/counters/water/importer"])
    fun importWaterCounters(
        @RequestPart file: MultipartFile,
    ): CounterInfoResponse {
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val totalSize = counterService.waterCounterParseAndSave(file, checkSum)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return CounterInfoResponse(file.originalFilename, totalSize)
    }

    data class CounterInfoResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
    )


    @Operation(summary = "Import answers from *.xlsx")
    @PostMapping(value = ["/answers/importer"])
    fun importAnswers(
        @RequestPart file: MultipartFile,
    ): AnswerInfoResponse {
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val totalSize = decisionService.parseAndSave(file, checkSum)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return AnswerInfoResponse(file.originalFilename, totalSize)
    }

    data class AnswerInfoResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
    )


    @Operation(summary = "Import contacts from *.xlsx")
    @PostMapping(value = ["/contacts/importer"])
    fun importContacts(
        @RequestPart file: MultipartFile,
    ): ContactInfoResponse {
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (totalSize, officeSize, flatSize, garageSize) = contactService.parseAndSave(file, checkSum)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return ContactInfoResponse(file.originalFilename, totalSize, officeSize, flatSize, garageSize)
    }

    data class ContactInfoResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
        val officeEmailSize: Int = 0,
        val flatEmailSize: Int = 0,
        val garageEmailSize: Int = 0,
    )


    @Operation(summary = "Import accounts from registry from *.xlsx")
    @PostMapping(value = ["/registry/importer"])
    fun importAccountsFromRegistry(
        @RequestPart file: MultipartFile,
    ): AccountRegistryResponse {
        fileService.isExtensionEqual(file, "xls")
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (totalSize, flatsWithCertSize, garagesOrOfficesWithCertSize) = roomService.parseAndSaveRegistry(
            file,
            checkSum
        )
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return AccountRegistryResponse(
            file.originalFilename,
            totalSize,
            flatsWithCertSize,
            garagesOrOfficesWithCertSize
        )
    }

    data class AccountRegistryResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
        val flatsWithCertSize: Int = 0,
        val garagesOrOfficesWithCertSize: Int = 0,
    )


    @Operation(summary = "Import accounts from \"HOMEOWNER\" from *.xlsx")
    @PostMapping(value = ["/homeowner/accounts/importer"])
    fun importAccountsFromHomeowners(
        @RequestPart file: MultipartFile,
    ): AccountHomeownersResponse {
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (roomSize, ownerSize, totalSquare, totalPercentage) = roomService.parseAndSave(file, checkSum)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return AccountHomeownersResponse(file.originalFilename, roomSize, ownerSize, totalSquare, totalPercentage)
    }

    data class AccountHomeownersResponse(
        val fileName: String? = "",
        val roomSize: Int = 0,
        val ownerSize: Int = 0,
        val totalSquare: BigDecimal = BigDecimal.ZERO,
        val totalPercentage: BigDecimal = BigDecimal.ZERO,
    )


    @Operation(summary = "Import counterparties from *.xlsx")
    @PostMapping(value = ["/counterparties/importer"])
    fun importCounterparties(
        @RequestPart file: MultipartFile
    ): CounterpartyInfoResponse {
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (size, numberOfUnique) = counterpartyService.parseAndSave(file)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return CounterpartyInfoResponse(file.originalFilename, size, numberOfUnique)
    }

    data class CounterpartyInfoResponse(
        val fileName: String? = "",
        val size: Int = 0,
        val numberOfUnique: Int = 0,
    )


    @Operation(summary = "Import payments from *.xlsx")
    @PostMapping(value = ["/payments/importer"])
    fun importPayments(
        @RequestPart file: MultipartFile
    ): PaymentInfoResponse {
        println("check")
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (totalSize, uniqTotalSize, incomingSize, outgoingSize, incomingSum, outgoingSum) = paymentService.parseAndSave(
            file,
            checkSum
        )
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum)
        return PaymentInfoResponse(
            file.originalFilename,
            totalSize,
            uniqTotalSize,
            incomingSize,
            outgoingSize,
            incomingSum,
            outgoingSum
        )
    }

    data class PaymentInfoResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
        val uniqTotalSize: Int = 0,
        val incomingSize: Int = 0,
        val outgoingSize: Int = 0,
        val incomingSum: BigDecimal? = BigDecimal.ZERO,
        val outgoingSum: BigDecimal? = BigDecimal.ZERO,
    )


    @Operation(summary = "Remove payments by file id")
    @DeleteMapping(value = ["/payments/{fileId}"])
    fun removePaymentsByFileId(
        @PathVariable fileId: Long
    ): Int = paymentService.removePaymentsByCheckSum(fileId = fileId, checksum = fileService.findById(fileId).checksum)

}