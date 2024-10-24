package ru.housekeeper.service.email

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import ru.housekeeper.utils.logger
import java.io.File

@Service
class EmailService(
    private val mailSender: JavaMailSender,
) {

    fun sendMessage(
        from: String = "dom@mr17dom1.ru",
        to: String,
        subject: String,
        body: String,
        html: Boolean = false,
    ): Boolean = sendMessageWithAttachment(from, to, subject, body, html = html)

    fun sendMessageWithAttachment(
        from: String = "dom@mr17dom1.ru",
        to: String,
        subject: String,
        body: String,
        attachmentFilename: String? = null,
        attachmentFile: File? = null,
        html: Boolean = false,
    ): Boolean {
        val message: MimeMessage = mailSender.createMimeMessage()
        return try {
            val helper = MimeMessageHelper(message, "utf-8")
            helper.setFrom(from)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(body, html)
            if (attachmentFilename != null && attachmentFile != null) {
                helper.addAttachment(attachmentFilename, attachmentFile)
            }
            logger().info("Send email to $to with subject $subject")
            mailSender.send(message)
            true
        } catch (e: Exception) {
            logger().error("Error send email to $to with subject $subject", e)
            false
        }
    }

}