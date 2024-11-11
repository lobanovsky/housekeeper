package ru.housekeeper.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.housekeeper.enums.UserRoleEnum
import java.time.LocalDateTime

@Entity
@Table(name = "\"user\"")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @CreationTimestamp
    @Column(updatable = false)
    val createDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val active: Boolean = true,

    @Column(unique = true)
    var email: String,

    var name: String,

    var description: String?,

    var password: String,

    var encodedPassword: String,

    @Column(unique = true)
    var code: String,

    @Enumerated(EnumType.STRING)
    var role: UserRoleEnum,

    //ref to workspace
    @Column
    var workspaces: List<Long> = listOf(),

    val ownerId: Long? = null,
)

