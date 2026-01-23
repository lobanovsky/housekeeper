package ru.housekeeper.experiment

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.writeLines

/**
 * Парсер файла реестра для ФИАС
 *
 * Файл получаем из программы "Домовладелец"
 *
 * На вход поступает общий файл, на основе которого формируются два новых файла
 * 1. ЖКУ (144 + 3 + 144 = 291)
 *    0000001001-0000001144 - квартиры + 0000002003, 0000002005, 0000002008 - коммерческие помещения
 *    0000003001-0000003144 - машиноместа
 * 2. Капитальный ремонт (144 + 3 + 144 = 291)
 *    0000500001-0000500144 - квартиры + 0000004164, 0000004165, 0000004166 - коммерческие помещения
 *    0000700001-0000700144 - машиноместа
 */
fun main2() {
    //9715357654_40703810338000004376_001_0105.txt
    //9715357654_40705810238000000478_001_0105
    val inn = "9715357654"
    val account = "40703810338000004376"
    val specialAccount = "40705810238000000478"
    val fileNumber = "0112"

    val prefixPath = "/Users/evgeny/Projects/tsn/housekeeper/etc/registry"
    val accountFileName = "${inn}_${account}_001_${fileNumber}.txt"
    val specialAccountFileName = "${inn}_${specialAccount}_001_${fileNumber}.txt"

    val paths = Paths.get("$prefixPath/$accountFileName")
    val lines = Files.readAllLines(paths, Charset.forName("windows-1251"))
    val data = lines.filterNot { it.contains("0000004019") }.map {
        val parts = it.split(";")
        Line(
            number = accountToNumber(parts[0]),
            account = parts[0],
            gisAccount = parts[1],
            fiasCode = parts[2],
            fullName = parts[3],
            address = parts[4],
            period = parts[5],
            amount = parts[6],
        )
    }
    println("Получено из файла строк: ${lines.size}")
    //ЖКУ (144 + 3 + 144 = 291)
    //0000001001-0000001144 - квартиры + 0000002003, 0000002005, 0000002008 - коммерческие помещения
    //0000003001-0000003144 - машиноместа

    //Капитальный ремонт (144 + 3 + 144 = 291)
    //0000500001-0000500144 - квартиры + 0000004164, 0000004165, 0000004166 - коммерческие помещения
    //0000700001-0000700144 - машиноместа

    //Итого 291 + 291 = 582

    val flats = data.filter { it.account.startsWith("0000001") }.sortedBy { it.number }
    update(flats, "кв", "flat", "кв.")
    val commercials = data.filter { it.account.startsWith("0000002") }.sortedBy { it.number }
    update(commercials, "офис", "office", "оф.")
    val parkingSpaces = data.filter { it.account.startsWith("0000003") }.sortedBy { it.number }
    update(parkingSpaces, "машиноместо", "parking", "мм.")

    assert(flats.size + commercials.size + parkingSpaces.size == 144 + 3 + 144)
    println("Всего ЖКУ: ${flats.size + commercials.size + parkingSpaces.size} строк")

    val capitalRepairFlats = data.filter { it.account.startsWith("0000500") }.sortedBy { it.number }
    update(capitalRepairFlats, "кв", "flat", "кв.")
    val capitalRepairCommercials = data.filter { it.account.startsWith("0000004") }.sortedBy { it.number }
    update(capitalRepairCommercials, "офис", "office", "оф.")
    val capitalRepairParkingSpaces = data.filter { it.account.startsWith("0000700") }.sortedBy { it.number }
    update(capitalRepairParkingSpaces, "машиноместо", "parking", "мм.")

    assert(capitalRepairFlats.size + capitalRepairCommercials.size + capitalRepairParkingSpaces.size == 144 + 3 + 144)
    println("Всего Кап.ремонт: ${capitalRepairFlats.size + capitalRepairCommercials.size + capitalRepairParkingSpaces.size} строк")

    println("Всего ЖКУ + Кап.ремонт: ${flats.size + commercials.size + parkingSpaces.size + capitalRepairFlats.size + capitalRepairCommercials.size + capitalRepairParkingSpaces.size} строк")

    //Создание файла ЖКУ
    Paths.get("$prefixPath/_${accountFileName}").writeLines(
        (flats + commercials + parkingSpaces).map { it.toLine() },
        Charset.forName("windows-1251")
    )
    //Создание файла Кап.ремонт
    Paths.get("$prefixPath/_${specialAccountFileName}").writeLines(
        (capitalRepairFlats + capitalRepairCommercials + capitalRepairParkingSpaces).map { it.toLine() },
        Charset.forName("windows-1251")
    )
}

private fun update(flats: List<Line>, type: String, prefix: String, shortPrefix: String) {
    flats.forEach {
        it.address =
            "г Москва, вн.тер.г. муниципальный округ Марьина роща, проезд 17-й Марьиной Рощи, д. 1, $type.${it.number}"
        it.fiasCode = "e7537376-4d22-4368-ae9b-003f5292adb3,$shortPrefix${it.number}"
        it.gisAccount = "gisAccount-$prefix.${it.number}"
    }
}

fun accountToNumber(account: String): Int? {
    if (account.startsWith("0000004")) {
        return when (account) {
            "0000004164" -> 3
            "0000004165" -> 5
            "0000004166" -> 8
            else -> null
        }
    }
    return account.trimStart('0').drop(1).trimStart('0').toInt()
}

data class Line(
    //last 3 characters and remove leading zeros
    var number: Int? = null,
    //мин. 3 знака макс. 50
    val account: String,
    var gisAccount: String,
    //100
    var fiasCode: String,
    //Фамилия, Имя, Отчество
    val fullName: String,
    //Населенный пункт, адрес, номер
    //дома, кв
    var address: String,
    //ММГГ
    val period: String,
    //999999,99
    val amount: String,


) {
    fun toLine() = "$account;$gisAccount;$fiasCode;$fullName;$address;$period;$amount"
}

//fun String.su() = this.substring(this.length - 3).toInt()