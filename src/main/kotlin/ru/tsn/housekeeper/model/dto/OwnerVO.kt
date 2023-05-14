package ru.tsn.housekeeper.model.dto

import ru.tsn.housekeeper.model.entity.Owner
import java.time.LocalDateTime

class OwnerVO(
    val fullName: String,
    private val emails: MutableSet<String> = mutableSetOf(),
    private var phones: MutableSet<String> = mutableSetOf(),
    private var active: Boolean = true,
    private var dateOfLeft: LocalDateTime? = null,
) {

    fun toOwner(checksum: String): Owner {
        return Owner(
            fullName = fullName,
            emails = emails,
            phones = phones,
            active = active,
            dateOfLeft = dateOfLeft,
            rooms = mutableSetOf(),
            source = checksum,
        )
    }
}