package ru.housekeeper.enums

enum class AnswerEnum(val answers: Set<String>) {
    YES(setOf("За", "1")),
    NO(setOf("Против", "0")),
    UNDEFINED(setOf("Воздержался", "2"));

    companion object {
        fun determine(s: String): AnswerEnum {
            for (v in values()) {
                if (v.answers.contains(s)) {
                    return v
                }
            }
            return UNDEFINED
        }
    }
}