package ru.housekeeper.service.email

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.TemplateService
import ru.housekeeper.utils.logger

@Service
class MailingService(
    private val mailService: MailService,
    private val ownerService: OwnerService,
    private val templateService: TemplateService,
) {

    @Async
    fun refusalOfPaperReceipts() {
        val owners = ownerService.findAll()
        val totalOwners = owners.filter { it.emails.isNotEmpty() }.size
        val totalEmails = owners.sumOf { it.emails.size }
        var totalSent = 0
        var totalNotSent = 0

        val template = templateService.findTemplateByName("refusal-of-paper-receipts")
        for (owner in owners) {
            owner.emails.forEach {
                val sent = mailService.sendMessage(
                    to = it,
                    subject = template?.subject ?: "",
                    body = template?.body?.replace("{{fullName}}", owner.fullName) ?: ""
                )
                if (sent) totalSent++ else totalNotSent++
            }
        }
        logger().info("Total owners $totalOwners, total emails $totalEmails, total sent $totalSent, total not sent $totalNotSent")
    }
}