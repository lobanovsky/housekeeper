package ru.housekeeper.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.housekeeper.model.entity.Template
import ru.housekeeper.repository.TemplateRepository
import ru.housekeeper.utils.entityNotfound

@Service
class TemplateService(
    private val templateRepository: TemplateRepository,
) {

    fun findAll(): List<Template> = templateRepository.findAll().toList()

//    fun getTemplateByType(type: MailTypeEnum): String? = templateRepository.findByType(type)?.body
//
//    fun createTemplate(template: TemplateController.TemplateRequest): Template {
//        val existTemplate = templateRepository.findByType(template.type)
//        if (existTemplate != null) throw IllegalArgumentException("Template with type ${template.type} already exist")
//        return templateRepository.save(Template(type = template.type, body = template.body))
//    }
//
//    fun updateTemplateById(id: Long, body: String): Template {
//        val existTemplate = templateRepository.findByIdOrNull(id) ?: entityNotfound("Template" to id)
//        existTemplate.body = body
//        return templateRepository.save(existTemplate)
//    }

    fun findTemplateById(id: Long): Template? = templateRepository.findByIdOrNull(id) ?: entityNotfound("Template" to id)

    fun deleteTemplateById(id: Long) = templateRepository.deleteById(id)

}
