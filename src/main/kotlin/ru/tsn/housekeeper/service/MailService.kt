package ru.tsn.housekeeper.service

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import ru.tsn.housekeeper.utils.logger
import java.io.File

@Service
class MailService(
    private val mailSender: JavaMailSender,
) {

    fun sendMessageWithAttachment(
        to: String,
        subject: String,
        body: String,
        attachmentFilename: String,
        attachmentFile: File
    ): Boolean {
        val message: MimeMessage = mailSender.createMimeMessage()
        return try {
            val helper = MimeMessageHelper(message, true)
            helper.setFrom("dom@mr17dom1.ru")
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(body)
            helper.addAttachment(attachmentFilename, attachmentFile)
            mailSender.send(message)
            true
        } catch (e: Exception) {
            logger().error("Error send email to $to: " + e.message)
            false
        }
    }

}