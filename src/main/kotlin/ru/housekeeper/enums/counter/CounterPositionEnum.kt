package ru.housekeeper.enums.counter

enum class CounterPositionEnum(val description: String){
    ONE("Одна-тарифный"),
    TWO("Двух-тарифный (Т1, Т2)"),
    THREE("Трёх-тарифный (Т1, Т2, Т3)"),
    FOUR("Четырёх-тарифный (Т1, Т2, Т3, Т4)"),
}