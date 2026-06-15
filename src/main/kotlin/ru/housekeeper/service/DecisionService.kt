package ru.housekeeper.service

import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.entity.Decision
import ru.housekeeper.model.entity.Room
import ru.housekeeper.parser.AnswerParser
import ru.housekeeper.repository.DecisionRepository
import ru.housekeeper.service.email.EmailService
import ru.housekeeper.utils.logger
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class DecisionService(
    private val templateService: TemplateService,
    private val ownerService: OwnerService,
    private val roomService: RoomService,
    private val decisionRepository: DecisionRepository,
    private val emailService: EmailService,
    private val docFileService: ru.housekeeper.docs.DocFileService,
    private val pdfFileService: ru.housekeeper.docs.PdfFileService,
) {

    fun getNotVoted(): List<Decision> =
        decisionRepository.findByVoted(voted = false).sortedByDescending { it.percentage }

    fun getPrint(): List<Decision> = decisionRepository.findByPrint(print = true).sortedByDescending { it.percentage }

    fun printDecision(path: String, withDocx: Boolean = false, getDecisions: () -> List<Decision>): Int {
        val decisions = getDecisions.invoke()
        logger().info("Number of printing decisions: ${decisions.size}, withDocx=$withDocx")

        decisions.forEach { decision ->
            val decisionLines = decision.blank.split("\n")
            val baseName = "Решение собственника (${decision.numbersOfRooms}) ${decision.fullName}"
            pdfFileService.doIt(
                rootPath = "etc",
                lines = decisionLines,
                path = path,
                fileName = "$baseName.pdf",
                ownerName = decision.fullName
            )
            if (withDocx) {
                docFileService.doIt(
                    rootPath = "etc",
                    lines = decisionLines,
                    path = path,
                    fileName = "$baseName.docx",
                    ownerName = decision.fullName
                )
            }
        }
        return decisions.size
    }


    fun findAll(): List<Decision> = decisionRepository.findAll().toList()

    fun findNotVoted(): List<Decision> = decisionRepository.findNotVoted()

    fun findNotNotifiedAndNotVoted(): List<Decision> =
        decisionRepository.findByNotifiedAndVoted(notified = false, voted = false)

    data class MailingInfo(
        val totalDecisions: Int,
        val totalEmails: Int,
        val sentDecisions: Int,
        val sentEmail: Int,
    )

    fun sendDecisions(templateId: Long, getDecisions: () -> List<Decision>): MailingInfo {
        val commonBody = templateService.findTemplateById(templateId)?.body ?: ""
        val totalDecisions = decisionRepository.countDecisions().toInt()
        val totalEmails = decisionRepository.findNotVoted().map { it.emails }.flatten().size
        val dateOfNotification = LocalDateTime.now()

        var numberOfMailsSent = 0
        var numberOfDecisionsSent = 0
        val decisions = getDecisions.invoke()
        logger().info("Number of decistions for email sending: ${decisions.size}")
        decisions.forEach { decision ->
            val subject = "ТСН МР17дом1. Решение собственника ${decision.fullName}. (${decision.numbersOfRooms})"
            val file = ru.housekeeper.docs.SimpleDocFileService().doIt(decision.blank.split("\n"))
            val attachmentFilename = "Решение собственника (${decision.numbersOfRooms}) ${decision.fullName}.docx"
            val attachmentFile = File("etc/blanks/$attachmentFilename")
            attachmentFile.writeBytes(file.toByteArray())
            decision.emails.forEach { email ->
                logger().info("Send ${numberOfMailsSent + 1} of $totalEmails")
                val result = emailService.sendMessageWithAttachment(
                    to = email,
                    subject = subject,
                    body = commonBody.replace("{{fullName}}", decision.fullName),
                    attachmentFilename = attachmentFilename,
                    attachmentFile = attachmentFile
                )
                logger().info("Status: $result: to $email, (${decision.numbersOfRooms})")
                if (result) {
                    decision.notified = true
                    decision.dateOfNotified = dateOfNotification
                    decisionRepository.save(decision)
                    numberOfMailsSent++
                }
            }
            numberOfDecisionsSent++
        }
        return MailingInfo(totalDecisions, totalEmails, numberOfDecisionsSent, numberOfMailsSent)
    }

    data class DecisionInfo(
        val totalSize: Int,
        val totalSquare: BigDecimal,
        val totalPercentage: BigDecimal,
    )

    //TODO to add skip duplicate
    fun prepareDecision(): DecisionInfo {
        val template = templateService.findTemplateById(5L)
        val header = template?.header?.split("\n") ?: emptyList()
        val body = template?.body?.split("\n") ?: emptyList()
        val footer = template?.footer?.split("\n") ?: emptyList()

        val existOwners = ownerService.findAll().filter { it.active }
//        val existOwners = listOf<OwnerEntity>(ownerService.findByFullName("Лобановский Евгений Владимирович"))
        val existRooms = roomService.findAll().associateBy { it.id }

        val decisions = mutableListOf<Decision>()
        val createDate = LocalDateTime.now()
        existOwners.forEach { owner ->
            val totalSquare = getTotalSquare(owner.rooms.toList(), existRooms)
            val percentageSquare = getPercentageSquare(owner.rooms.toList(), existRooms)
            decisions.add(
                Decision(
                    uuid = "${owner.rooms} $totalSquare $percentageSquare",
                    fullName = owner.fullName,
                    square = totalSquare,
                    percentage = percentageSquare,
                    rooms = owner.rooms,
                    emails = owner.emails.toMutableSet(),
                    blank = listOf(header, makeBlank(owner.rooms.toList(), existRooms), body, footer).flatten()
                        .joinToString("\n"),
                    numbersOfRooms = getRoomNumbers(owner.rooms.toList(), existRooms),
                    createDate = createDate,
                )
            )
        }
        decisionRepository.saveAll(decisions)
        return DecisionInfo(
            totalSize = decisions.size,
            totalSquare = decisions.sumOf { it.square },
            totalPercentage = decisions.sumOf { it.percentage },
        )
    }

    private fun getRoomNumbers(rooms: List<Long?>, existRooms: Map<Long?, Room>): String {
        val typeAndNumbersOfRooms = mutableListOf<String>()
        val sortedRooms = rooms.mapNotNull { existRooms[it] }.sortedBy { it.type }.sortedByDescending { it.number }
        sortedRooms.forEach { room ->
            val number = room.number
            when (room.type) {
                RoomTypeEnum.FLAT -> typeAndNumbersOfRooms.add("кв.${number.padStart(3, '0')}")
                RoomTypeEnum.GARAGE -> typeAndNumbersOfRooms.add("мм.${number.padStart(3, '0')}")
                RoomTypeEnum.OFFICE -> typeAndNumbersOfRooms.add("оф.${number.padStart(3, '0')}")
            }
        }
        return typeAndNumbersOfRooms.sortedBy { it }.joinToString(", ")
    }

    private fun makeBlank(rooms: List<Long?>, existRooms: Map<Long?, Room>): List<String> {
        val ownerName = existRooms[rooms.first()]?.ownerName ?: ""
        val roomLines = rooms.mapNotNull { existRooms[it] }.map { room ->
            val cert = if (room.certificate?.isNotBlank() == true) ", ${room.certificate}" else ""
            val cadastre = if (room.cadastreNumber?.isNotBlank() == true) ", кад.№ ${room.cadastreNumber}" else ""
            val description = room.type.description
            val number = room.number
            val square = room.square
            val percentage = room.percentage
            "$description № ${number}, ${square}кв.м, доля: ${percentage}%$cert$cadastre"
        }.sortedBy { it }
        return listOf(ownerName) + roomLines
    }

    private fun getTotalSquare(rooms: List<Long?>, existRooms: Map<Long?, Room>) =
        rooms.map { existRooms[it]?.square }.sumOf { it ?: BigDecimal.ZERO }

    private fun getPercentageSquare(rooms: List<Long?>, existRooms: Map<Long?, Room>) =
        rooms.map { existRooms[it]?.percentage }.sumOf { it ?: BigDecimal.ZERO }

    fun parseAndSave(file: MultipartFile, checkSum: String): Int {
        val answers = AnswerParser(file).parse()
        logger().info("Parsed ${answers.size} answers")
        for (answer in answers) {
            val decision = decisionRepository.findByIdOrNull(answer.decisionId)
            decision?.answers = answer.answers
            decision?.voted = true
            decision?.let { decisionRepository.save(it) }
        }
        return answers.size
    }
}
