package ru.tsn.housekeeper.model.entity.gate

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.tsn.housekeeper.enums.gate.LogEntryAccessMethodEnum
import ru.tsn.housekeeper.enums.gate.LogEntryStatusEnum
import java.time.LocalDateTime

@Entity
@Table(
    name = "log_entry",
    indexes = [Index(name = "idx_in_date_time", columnList = "dateTime"),
        Index(name = "idx_in_status", columnList = "status"),
        Index(name = "idx_in_flat_number", columnList = "flatNumber"),
        Index(name = "idx_in_phone_number", columnList = "phoneNumber"),
        Index(name = "idx_in_custom_id", columnList = "customId")]
)
class LogEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreationTimestamp
    @Column(updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now(),

    val source: String,

    val gateId: Long,

    val gateName: String,

    @Column(updatable = false)
    val dateTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    val status: LogEntryStatusEnum,

    val userName: String?,

    val flatNumber: String?,

    val cell: String?,

    @Enumerated(EnumType.STRING)
    val method: LogEntryAccessMethodEnum?,

    val phoneNumber: String?,

    val line: String,

    @Column(updatable = false, unique = true)
    val customId: String,
)