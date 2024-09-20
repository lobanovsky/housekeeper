package ru.housekeeper.utils

fun String.replaceLatinToCyrillic(): String {
    // Создаем маппинг латинских букв на соответствующие русские
    val latinToCyrillicMap = mapOf(
        'A' to 'А', 'B' to 'В', 'E' to 'Е', 'K' to 'К',
        'M' to 'М', 'H' to 'Н', 'O' to 'О', 'P' to 'Р',
        'C' to 'С', 'T' to 'Т', 'Y' to 'У', 'X' to 'Х'
    )
    // Преобразуем строку
    return this.map { char ->
        // Приводим символ к верхнему регистру и ищем в маппинге
        latinToCyrillicMap[char.uppercaseChar()] ?: char // Заменяем, если находим, или оставляем символ
    }.joinToString("").lowercase() // Приводим результат к нижнему регистру
}

fun isValidRussianCarNumber(carNumber: String): Boolean {
    // Регулярное выражение для проверки формата номера
    val regex = Regex("^[АВЕКМНОРСТУХ]\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}\$")

    // Проверяем соответствие регулярному выражению
    return regex.matches(carNumber.uppercase())
}


fun isValidBelarusCarNumber(carNumber: String): Boolean {
    // Регулярные выражения для разных форматов номеров
    val patterns = listOf(
        Regex("^(\\d{4})\\s([АВЕІКМНОРСТХ]{2})-(\\d)$"), // Легковые автомобили: 0000 ХХ-0
        Regex("^[АВЕІКМНОРСТХ]{2}\\s(\\d{4})-(\\d)$"), // Грузовые и автобусы: ХХ 0000-0
        Regex("^[АВЕІКМНОРСТХ]{2}-(\\d)\\s(\\d{4})$"), // Грузовые и автобусы нестандартное размещение: ХХ-0 0000
        Regex("^[АВЕІКМНОРСТХ]{1}\\s(\\d{4})\\s([АВЕІКМНОРСТХ])-(\\d)$") // Прицепы и полуприцепы: Х 0000 Х-0
    )

    // Проверяем соответствие хотя бы одному из паттернов
    return patterns.any { it.matches(carNumber.uppercase()) }
}
