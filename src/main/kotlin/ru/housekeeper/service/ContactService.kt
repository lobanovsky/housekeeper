package ru.housekeeper.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.housekeeper.enums.RoomTypeEnum
import ru.housekeeper.model.dto.ContactVO
import ru.housekeeper.model.entity.Contact
import ru.housekeeper.parser.ContactParser
import ru.housekeeper.repository.ContactRepository
import ru.housekeeper.repository.room.RoomRepository
import ru.housekeeper.utils.logger

@Service
class ContactService(
    private val roomRepository: RoomRepository,
    private val ownerService: OwnerService,
    private val contactRepository: ContactRepository,
) {

    data class ContactsInfo(
        val totalSize: Int,
        val officeEmailSize: Int,
        val flatEmailSize: Int,
        val garageEmailSize: Int,
    )

    @Transactional
    fun parseAndSave(file: MultipartFile, checkSum: String): ContactsInfo {
        val contactVOs = ContactParser(file).parse()

        val contacts = contactVOs.map { it.toContact() }
        upsert(contacts)

        val owners = contactVOs.filter { !it.block && !it.tenant }
        RoomTypeEnum.entries.forEach { type ->
            updateOwner(owners.filter { it.roomType == type }.groupBy { it.label }, type)
        }

        return ContactsInfo(
            contactVOs.size,
            counterByType(owners, RoomTypeEnum.OFFICE),
            counterByType(owners, RoomTypeEnum.FLAT),
            counterByType(owners, RoomTypeEnum.GARAGE)
        )
    }

    private fun counterByType(contacts: List<ContactVO>, type: RoomTypeEnum) = contacts.filter { it.roomType == type }.size

    private fun updateOwner(contacts: Map<String?, List<ContactVO?>>, type: RoomTypeEnum) {
        contacts.forEach { (label, contact) ->
            val room = roomRepository.findByNumberAndType(label, type)
            room?.owners?.filterNotNull()?.forEach {
                ownerService.findById(it).let { owner ->
                    owner?.emails?.addAll(contact.filter { c -> c?.email?.isNotBlank() == true }.mapNotNull { c -> c?.email?.lowercase() })
                    owner?.phones?.addAll(contact.filter { c -> c?.phone?.isNotBlank() == true }.mapNotNull { c -> c?.phone })
                }
            }
        }
    }

    /**
     *     private fun removeDuplicates(payments: List<Payment>, savedUUIDs: () -> Set<String>): List<Payment> {
     *         val saved = savedUUIDs()
     *         val uploaded = payments.map { it.uuid }.toSet()
     *         val duplicates = uploaded intersect saved
     *         logger().info("Uploaded ${uploaded.size}, unique -> ${(uploaded subtract saved).size}")
     *         val groupedPayments = payments.associateBy { it.uuid }.toMutableMap()
     *         duplicates.forEach(groupedPayments::remove)
     *         return groupedPayments.values.toList()
     *     }
     */

    private fun upsert(contacts: List<Contact>) {
        val existContacts = contactRepository.findAll()
        val existPhones = existContacts.map { it.phone }.toSet()
        val uploadedPhones = contacts.map { it.phone }.toSet()
        val duplicates = uploadedPhones intersect existPhones
        logger().info("Uploaded ${uploadedPhones.size}, unique -> ${(uploadedPhones subtract existPhones).size}")
        val groupedContacts = contacts.associateBy { it.phone }.toMutableMap()
        duplicates.forEach(groupedContacts::remove)
    }

}