package ru.housekeeper.model.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import ru.housekeeper.enums.AnswerEnum
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "decision")
class Decision(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    var uuid: String = "",

    val fullName: String,

    val square: BigDecimal = BigDecimal.ZERO,

    val percentage: BigDecimal = BigDecimal.ZERO,

    @Type(JsonType::class)
    @Column(name = "rooms", columnDefinition = "jsonb")
    val rooms: MutableSet<Long?> = mutableSetOf(),

    val numbersOfRooms: String,

    @Type(JsonType::class)
    @Column(name = "emails", columnDefinition = "jsonb")
    val emails: MutableSet<String> = mutableSetOf(),

    @Column(name = "notified")
    var notified: Boolean = false,

    var dateOfNotified: LocalDateTime? = null,

    @Column(name = "voted")
    val voted: Boolean = false,

    @Column(name = "print")
    val print: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var blank: String,

    @Column(updatable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Type(JsonType::class)
    @Column(name = "answers", columnDefinition = "jsonb")
    var answers: List<AnswerEnum>? = listOf(),

    )