package ru.housekeeper.service

import org.springframework.core.io.ResourceLoader
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import ru.housekeeper.utils.logger
import kotlin.io.readBytes
import kotlin.text.replace

@Service
class EmailService(
    private val emailSender: JavaMailSender,
    private val resourceLoader: ResourceLoader,
) {

    fun sendInvitation(
        email: String,
        name: String,
        password: String,
        code: String
    ) {
        logger().info("Sending invitation email for $email")

        val message = emailSender.createMimeMessage()
        val messageHelper = MimeMessageHelper(message, false, "utf-8")
        messageHelper.setText(
            String(resourceLoader.getResource("classpath:email/invitation.html").inputStream.readBytes())
                .replace("{{host}}", "https://housekeeper.docduck.io")
                .replace("{{login}}", email)
                .replace("{{password}}", password)
                .replace("{{code}}", code), true
        )
        messageHelper.setFrom("mr17dom1@yandex.ru")
        messageHelper.setTo(email)
        //for debugging
        messageHelper.setBcc("e.lobanovsky@ya.ru")
        messageHelper.setSubject("Приглашение в сервис Хаускипер")
        message.saveChanges()

        emailSender.send(message)
        logger().info("Email for $email has been sent")
    }

}
