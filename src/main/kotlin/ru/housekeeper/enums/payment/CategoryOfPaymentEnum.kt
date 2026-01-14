package ru.housekeeper.enums.payment

enum class CategoryOfPaymentEnum(val description: String) {
    RESOURCES("РСО"),

    GARBAGE_COLLECTION("Вывоз мусора"),

    ELEVATOR("Техобслуживание лифтов"),

    FIRE_SAFETY("Пожарная безопасность"),

    EMERGENCY_SITUATIONS("МЧС"),

    INDIVIDUAL_HEAT_POINT("ИТП"),

    METERING_DEVICES("АИИС КУЭ"),

    SOFTWARE("Программное обеспечение"),

    CELLULAR("Сотовая связь"),

    INSURANCE("Обязательное страхование"),

    BANK("Банк"),

    TAX("Налоги"),

    STAFF_SALARY("Зарплата сотрудников"),

    MANAGEMENT_SALARY("Зарплата правления"),

    GATE_REPAIR("Ремонт ворот и дверей"),

    ELECTRICITY_REPAIR("Ремонт электрики"),

    CLEANING_PRODUCTS("Cредства для уборки"),

    DISPATCHING("Диспетчеризация"),

    ELECTRONIC_DEVICES("Электронные устройства"),

    PARKING_REPAIR("Ремонт парковки"),

    MAINTENANCE_FLOOR_WASHING_MACHINE("Техническое обслуживание моющей машины"),

    DIRT_PROOF_MATS("Грязезащитные ковры"),

    MAINTENANCE_KARCHER("Обслуживание KARCHER"),

    GARDEN("Сад и огород"),

    FURNITURE("Мебель"),

    LEGAL_SERVICES("Юридические услуги"),

    PARKING_VIDEO_SURVEILLANCE("Видеонаблюдение паркинга"),

    MAINTENANCE("Содержание и ремонт"),

    UNDER_THE_REPORT("Под отчёт"),

    COURT_COSTS("Судебные издержки"),

    CLEANING_PARKING("Уборка паркинга"),

    CLEANING("Уборка"),

    AIR_FILTERS("Воздушные фильтры"),

    OPERATIONAL_TESTS("Эксплуатационные испытания"),

    ALARMS("Сигнализации"),

    EDO("Электронный документооборот (ЭДО)"),

    DESIGN_DOCUMENTATION("Проектная документация"),

    WATER_CHEMICAL_ANALYSIS("Химический анализ воды"),

    HEATING("Отопление"),
    WATER_SUPPLY("Водоснабжение"),
    ELECTRICITY_SUPPLY("Электроснабжение"),

    OVERHAUL("Капитальный ремонт")

}