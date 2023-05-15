package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.housekeeper.enums.RoomTypeEnum
import java.time.LocalDateTime


@Entity
@Table(name = "contact")
class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val label: String? = null,

    val numbersOfRooms: String,

    val fullName: String? = null,

    val phone: String? = null,

    @Enumerated(EnumType.STRING)
    val roomType: RoomTypeEnum = RoomTypeEnum.FLAT,

    val block: Boolean,

    val blockedDate: LocalDateTime? = null,

    val tenant: Boolean,

    val carNumber: String? = null,

    val car: String? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    )