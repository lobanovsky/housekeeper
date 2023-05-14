package ru.tsn.housekeeper.model.dto

import ru.tsn.housekeeper.enums.AnswerEnum

data class AnswerVO(
    val decisionId: Long,
    val fullName: String,
    val answers: List<AnswerEnum>,
)