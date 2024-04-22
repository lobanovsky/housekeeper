package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.entity.Decision
import ru.housekeeper.model.entity.Room
import ru.housekeeper.parser.AnswerParser
import ru.housekeeper.repository.DecisionRepository
import ru.housekeeper.service.email.MailService
import ru.housekeeper.utils.logger
import ru.housekeeper.utils.yyyyMMddHHmmssDateFormat
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class DecisionService(
    private val templateService: TemplateService,
    private val ownerService: OwnerService,
    private val roomService: RoomService,
    private val decisionRepository: DecisionRepository,
    private val mailService: MailService,
    private val docFileService: ru.housekeeper.docs.DocFileService,
) {

    fun getNotVoted(): List<Decision> = decisionRepository.findByVoted(voted = false).sortedByDescending { it.percentage }

    fun getPrint(): List<Decision> = decisionRepository.findByPrint(print = true).sortedByDescending { it.percentage }

    fun printDecision(path: String, f: () -> List<Decision>): Int {
        val notVotedDecisions = f.invoke()
        logger().info("notVotedDecisions: ${notVotedDecisions.size}")
        val allDecisionLines = mutableListOf<String>()

        notVotedDecisions.forEach { decision ->
            val blank = decision.blank
            val decisionLines = blank.split("\n")
            allDecisionLines.addAll(decisionLines)
        }
        val fileName = "Decisions_${LocalDateTime.now().format(yyyyMMddHHmmssDateFormat())}"
        docFileService.doIt(
            rootPath = "etc",
            lines = allDecisionLines,
            path = path,
            fileName = fileName
        )
        return notVotedDecisions.size
    }


    fun findAll(): List<Decision> = decisionRepository.findAll().toList()

    fun findNotVoted(): List<Decision> = decisionRepository.findNotVoted()

    fun findNotNotifiedAndNotVoted(): List<Decision> = decisionRepository.findByNotifiedAndVoted(notified = false, voted = false)

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
        getDecisions.invoke().forEach { decision ->
            val subject = "ТСН МР17дом1. Решение собственника ${decision.fullName}. (${decision.numbersOfRooms})"
            val file = ru.housekeeper.docs.SimpleDocFileService().doIt(decision.blank.split("\n"))
            val attachmentFilename = "Решение собственника (${decision.numbersOfRooms}) ${decision.fullName}.docx"
            val attachmentFile = File("etc/blanks/$attachmentFilename")
            attachmentFile.writeBytes(file.toByteArray())
            decision.emails.forEach { email ->
                logger().info("Send ${numberOfMailsSent + 1} of $totalEmails")
                val result = mailService.sendMessageWithAttachment(
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
        val footer = template?.footer?.split("\n") ?: emptyList()

        val existOwner = ownerService.findAll()
        val existRooms = roomService.findAll().associateBy { it.id }

        val decisions = mutableListOf<Decision>()
        val createDate = LocalDateTime.now()
        existOwner.forEach { owner ->
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
                    blank = listOf(header, makeBlank(owner.rooms.toList(), existRooms), footer).flatten().joinToString("\n"),
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
        val lines = mutableListOf(existRooms[rooms.first()]?.ownerName ?: "")
        val sortedRooms = rooms.mapNotNull { existRooms[it] }
        sortedRooms.forEach { room ->
            val cert = if (room.certificate?.isNotBlank() == true) ", ${room.certificate}" else ""
            val description = room.type.description
            val number = room.number
            val square = room.square
            val percentage = room.percentage
            lines.add("$description № ${number}, ${square}кв.м, доля: ${percentage}%$cert")
        }
        return lines.sortedBy { it }
    }

    private fun getTotalSquare(rooms: List<Long?>, existRooms: Map<Long?, Room>) = rooms.map { existRooms[it]?.square }.sumOf { it ?: BigDecimal.ZERO }

    private fun getPercentageSquare(rooms: List<Long?>, existRooms: Map<Long?, Room>) = rooms.map { existRooms[it]?.percentage }.sumOf { it ?: BigDecimal.ZERO }

    fun parseAndSave(file: MultipartFile, checkSum: String): Int {
        val answers = AnswerParser(file).parse()
        logger().info("Parsed ${answers.size} answers")
        for (answer in answers) {
            val decision = decisionRepository.findByIdOrNull(answer.decisionId)
            decision?.answers = answer.answers
            decision?.let { decisionRepository.save(it) }
        }
        return answers.size
    }
}
