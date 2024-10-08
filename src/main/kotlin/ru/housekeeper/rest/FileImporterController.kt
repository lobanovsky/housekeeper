package ru.housekeeper.rest

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.FileTypeEnum
import ru.housekeeper.enums.counter.CounterTypeEnum
import ru.housekeeper.service.*
import ru.housekeeper.service.counter.CounterService
import ru.housekeeper.service.gate.LogEntryService
import ru.housekeeper.utils.logger
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.time.Year

@CrossOrigin
@RestController
@RequestMapping("/files")
class FileImporterController(
    private val fileService: FileService,
    private val counterpartyService: CounterpartyService,
    private val paymentService: PaymentService,
    private val roomService: RoomService,
    private val decisionService: DecisionService,
    private val counterService: CounterService,
    private val logEntryService: LogEntryService,
) {

    @Operation(summary = "Import eldes gate from *.log")
    @PostMapping(value = ["/eldes-gate/importer"])
    fun importEldesGate(
        @RequestPart file: MultipartFile,
    ): EldesGateInfoResponse {
        logger().info("Try to upload [${file.originalFilename}]")
        fileService.isExtensionEqual(file, "log")
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val imei = file.originalFilename?.substringAfter("IMEI")?.substringBefore("_")
            ?: throw IllegalArgumentException("File name must contain IMEI")
        val (totalSize) = logEntryService.parseAndSave(file, checkSum, imei)
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.LOG_ENTRY)
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
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.COUNTER_WATER_VALUES)
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
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.DECISION_ANSWERS)
        return AnswerInfoResponse(file.originalFilename, totalSize)
    }

    data class AnswerInfoResponse(
        val fileName: String? = "",
        val totalSize: Int = 0,
    )


    @Operation(summary = "Import accounts from registry from *.xlsx")
    @PostMapping(value = ["/registry/importer"])
    fun importAccountsFromRegistry(
        @RequestPart file: MultipartFile,
    ): AccountRegistryResponse {
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (totalSize, flatsWithCertSize, garagesOrOfficesWithCertSize) = roomService.parseAndSaveRegistry(
            file,
            checkSum
        )
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.REGISTRIES)
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


//    @Operation(summary = "Import accounts from \"HOMEOWNER\" from *.xlsx")
//    @PostMapping(value = ["/homeowner/accounts/importer"])
//    fun importAccountsFromHomeowners(
//        @RequestPart file: MultipartFile,
//    ): AccountHomeownersResponse {
//        fileService.isExtensionEqual(file)
//        val checkSum = fileService.isDuplicateAndGetChecksum(file)
//        val (roomSize, ownerSize, totalSquare, totalPercentage) = roomService.parseAndSave(file, checkSum)
//        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.ACCOUNTS)
//        return AccountHomeownersResponse(file.originalFilename, roomSize, ownerSize, totalSquare, totalPercentage)
//    }

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
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.COUNTERPARTIES)
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
        fileService.isExtensionEqual(file)
        val checkSum = fileService.isDuplicateAndGetChecksum(file)
        val (totalSize, uniqTotalSize, incomingSize, outgoingSize, incomingSum, outgoingSum) = paymentService.parseAndSave(
            file,
            checkSum
        )
        fileService.saveFileInfo(file.originalFilename ?: "", file.size, checkSum, FileTypeEnum.PAYMENTS)
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

}