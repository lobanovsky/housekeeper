package ru.housekeeper.enums

enum class RoomTypeEnum(val description: String, val shortDescription: String) {
    FLAT("Квартира", "кв."),
    GARAGE("Машиноместо", "мм."),
    OFFICE("Коммерческое помещение", "оф.")
}