package ru.housekeeper.service.email

import org.springframework.core.io.ResourceLoader
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Room
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
import ru.housekeeper.service.TemplateService
import ru.housekeeper.utils.logger

@Service
class MailingService(
    private val emailService: EmailService,
    private val ownerService: OwnerService,
    private val roomService: RoomService,
    private val templateService: TemplateService,
    private val resourceLoader: ResourceLoader,
) {

    fun ping(email: String) {
        emailService.sendMessage(
            to = email,
            subject = "Ping",
            body = "Ping"
        )
    }

    fun sendInvitation(
        email: String,
        name: String,
        password: String,
        code: String
    ) {
        logger().info("Sending invitation email for $email")
        emailService.sendMessage(
            to = email,
            subject = "Приглашение в сервис Хаускипер",
            body = String(resourceLoader.getResource("classpath:email/invitation.html").inputStream.readBytes())
                .replace("{{host}}", "https://housekpr.ru")
                .replace("{{login}}", email)
                .replace("{{password}}", password)
                .replace("{{code}}", code),
            html = true
        )
        logger().info("Email for $email has been sent")
    }

    @Async
    fun refusalOfPaperReceipts() {
        val existRooms = roomService.findAll().associateBy { it.id }
        val owners = ownerService.findAll()
        val totalOwners = owners.filter { it.emails.isNotEmpty() }.size
        val totalEmails = owners.sumOf { it.emails.size }
        var totalSent = 0
        var totalNotSent = 0

        val template = templateService.findTemplateByName("refusal-of-paper-receipts")
        for (owner in owners) {
            val rooms = getRooms(owner.rooms.toList(), existRooms)
            owner.emails.forEach {
                val sent = emailService.sendMessage(
                    to = it,
                    subject = template?.subject ?: "",
                    body = template?.body
                        ?.replace("{{fullName}}", owner.fullName)
                        ?.replace("{{rooms}}", rooms.joinToString("\n")) ?: ""
                )
                if (sent) totalSent++ else totalNotSent++
            }
        }
        logger().info("Total owners $totalOwners, total emails $totalEmails, total sent $totalSent, total not sent $totalNotSent")
    }

    private fun getRooms(rooms: List<Long?>, existRooms: Map<Long?, Room>): List<String> {
        val lines = mutableListOf<String>()
        val sortedRooms = rooms.mapNotNull { existRooms[it] }
        sortedRooms.forEach { room ->
            val description = room.type.description
            val number = room.number
            val square = room.square
            lines.add("$description № ${number}, ${square} кв.м")
        }
        return lines.sortedBy { it }
    }

}