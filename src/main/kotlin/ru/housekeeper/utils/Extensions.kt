package ru.housekeeper.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

inline fun <reified T> T.logger(): Logger =
    LoggerFactory.getLogger(if (T::class.isCompanion) T::class.java.enclosingClass else T::class.java)

/**
 *You can use this method to get date time with zone id
 * LocalDateTime.now(ZoneId.of("Europe/Moscow").asDate() - return Date() with ZoneId
 */
fun LocalDateTime?.asDate() = this?.atZone(ZoneId.systemDefault())?.toInstant()?.let { Date.from(it) }

fun <T> printFirstFive(list: Collection<T>): String {
    val maxSize = 5
    return if (list.size > maxSize) list.take(maxSize)
        .toString() + "... and ${list.size - maxSize} items" else list.toString()
}

fun yyyyMMddDateFormat(): DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun yyyyMMddHHmmssDateFormat(): DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

fun String.onlyLettersAndNumber() = Regex("[^А-Яа-яA-Za-z0-9]").replace(this, "").lowercase()

fun String.onlyNumbers() = Regex("[^0-9]").replace(this, "")

fun String.isNumeric(): Boolean = Regex("-?[0-9]+(\\.[0-9]+)?").matches(this)

fun String.beautifulPhonePrint() = this.replaceFirst("(\\d{1})(\\d{3})(\\d{3})(\\d{2})(\\d{2})".toRegex(), "+$1-$2-$3-$4-$5")

fun String.onlyCyrillicLettersAndNumbers(): String = this
    .replace("[^а-яА-Я0-9]".toRegex(), "")
    .trim()
    .lowercase()

fun List<String>.sqlSeparator() = this.joinToString(separator = ",") { "'$it'" }

fun String.removeSpaces() = this.replace(" ", "")

fun String.simplify() = this
    .replace("\"", "")
    .replace("?", "")
    .trim()
    .lowercase()
    .replace(" +".toRegex(), " ")

fun String.color(): String {
    val hash = this.hashCode()
    val r = (hash and 0xFF0000 shr 16)
    val g = (hash and 0x00FF00 shr 8)
    val b = (hash and 0x0000FF)
    return "rgb($r, $g, $b)"
}
