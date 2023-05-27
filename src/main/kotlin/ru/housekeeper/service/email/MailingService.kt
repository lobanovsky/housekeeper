package ru.housekeeper.service.email

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Room
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
import ru.housekeeper.service.TemplateService
import ru.housekeeper.utils.logger

@Service
class MailingService(
    private val mailService: MailService,
    private val ownerService: OwnerService,
    private val roomService: RoomService,
    private val templateService: TemplateService,
) {

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
                val sent = mailService.sendMessage(
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