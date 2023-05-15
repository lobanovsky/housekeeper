package ru.housekeeper.model.dto

import ru.housekeeper.enums.AnswerEnum

data class AnswerVO(
    val decisionId: Long,
    val fullName: String,
    val answers: List<AnswerEnum>,
)