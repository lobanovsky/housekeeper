package ru.housekeeper.service.registry

import ru.housekeeper.model.entity.payment.IncomingPayment
import ru.housekeeper.utils.getFlatAccount
import ru.housekeeper.utils.getParkingAccount

//Поиск Лицевого счета в строке назначения платежа по правилам
fun findAccountByRules(payment: IncomingPayment): String? {

    //flats

    if (payment.fromName.contains("ЕПИФАНОВА НАДЕЖДА ЕВГЕНЬЕВНА", true))
        return getFlatAccount(4)

    if (payment.fromName.contains("Таланова Наталья Алексеевна", true))
        return getFlatAccount(11)

    if (payment.fromName.contains("БЕССОНОВА ОЛЬГА ПАВЛОВНА", true))
        return getFlatAccount(21)

    if (payment.fromName.contains("ВИНОГРАДОВ ГЕННАДИЙ АНДРЕЕВИЧ", true)
        && payment.purpose.contains("КВАРПЛАТА, КВ 68", true)
    ) return getFlatAccount(68)

    if (payment.fromName.contains("МУХТАРОВА НАТАЛЬЯ ТОФИКОВНА", true)
        && payment.purpose.contains("КВАРПЛАТА, НДС НЕ ОБЛАГАЕТСЯ", true)
    ) return getFlatAccount(76)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Квартира №30", true)
    ) return getFlatAccount(30)

    if (payment.fromName.contains("ЛУЦКИЙ АЛЕКСЕЙ АЛЕКСАНДРОВИЧ", true)
        && payment.purpose.contains("кв. 107", true)
    ) return getFlatAccount(107)

    if (payment.fromName.contains("ТАТЬЯНА АНАТОЛЬЕВНА СОФРОНОВА", true))
        return getFlatAccount(72)

    if (payment.fromName.contains("ЧЕКУНОВ ДМИТРИЙ ЮРЬЕВИЧ", true)
    ) return getFlatAccount(106)

    if (payment.fromName.contains("Коровина Любовь Николаевна", true)
    ) return getFlatAccount(9)

    if (payment.fromName.contains("САРКИСЯН КСЕНИЯ ВЛАДИМИРОВНА", true)
        && payment.purpose.contains("КВАРТИРА 46", true)
    ) return getFlatAccount(46)

    if (payment.fromName.contains("КАЗАДАЕВ ДМИТРИЙ ВИКТОРОВИЧ", true)
        && payment.purpose.contains("кв 103", true)
    ) return getFlatAccount(103)

    if (payment.fromName.contains("Васильева Елена Алексеевна", true)
    ) return getFlatAccount(39)

    if (payment.fromName.contains("СПЕКТОР МАРИНА РОМАНОВНА", true)
    ) return getFlatAccount(14)

    if (payment.fromName.contains("АБРАМЕНКОВ ДМИТРИЙ АЛЕКСАНДРОВИЧ", true)
        && payment.purpose.contains("ЛС №1130", true)
    ) return getFlatAccount(130)

    if (payment.fromName.contains("Половодов Виктор Павлович", true)
        && payment.purpose.contains("Коммунальные платежи", true)
    ) return getFlatAccount(41)


    //parking
    if (payment.fromName.contains("Оболёшев Сергей Леонидович", true)
        && payment.purpose.contains("ММ", true)
    ) return getParkingAccount(42)

    if (payment.fromName.contains("ВИНОГРАДОВ ГЕННАДИЙ АНДРЕЕВИЧ", true)
        && payment.purpose.contains("КВАРПЛАТА, М/М 79", true)
    ) return getParkingAccount(79)

    if (payment.fromName.contains("ЛИПЕЦКИЙ ИВАН ВЛАДИМИРОВИЧ", true)
        && payment.purpose.contains("м/м 109", true)
    ) return getParkingAccount(109)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машиноместо №34", true)
    ) return getParkingAccount(34)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машино-место №34", true)
    ) return getParkingAccount(34)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машиноместо №35", true)
    ) return getParkingAccount(35)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машино-место №35", true)
    ) return getParkingAccount(35)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машиноместо №58", true)
    ) return getParkingAccount(68)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машиноместо №68", true)
    ) return getParkingAccount(68)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машино-место №68", true)
    ) return getParkingAccount(68)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машиноместо №67", true)
    ) return getParkingAccount(67)

    if (payment.fromName.contains("Бобровский Николай Эдуардович", true)
        && payment.purpose.contains("Машино-место №67", true)
    ) return getParkingAccount(67)

    if (payment.fromName.contains("ЛУЦКИЙ АЛЕКСЕЙ АЛЕКСАНДРОВИЧ", true)
        && payment.purpose.contains("м/м 138", true)
    ) return getParkingAccount(138)

    if (payment.fromName.contains("Боев Роман Борисович", true)
        && payment.purpose.contains("А/м 105 Л/с плательщика: 3105", true)
    ) return getParkingAccount(105)

    if (payment.fromName.contains("Хлебникова Светлана Александровна", true)
        && payment.purpose.contains("а/м 123", true)
    ) return getParkingAccount(123)

    if (payment.fromName.contains("Козлов Евгений Вячеславович", true)
        && payment.purpose.contains("мм.38", true)
    ) return getParkingAccount(38)

    if (payment.fromName.contains("Козлов Евгений Вячеславович", true)
        && payment.purpose.contains("м/м 38", true)
    ) return getParkingAccount(38)

    if (payment.fromName.contains("Козлов Евгений Вячеславович", true)
        && payment.purpose.contains("мм.136", true)
    ) return getParkingAccount(136)

    if (payment.fromName.contains("Козлов Евгений Вячеславович", true)
        && payment.purpose.contains("м/м 136", true)
    ) return getParkingAccount(136)

    if (payment.fromName.contains("Хайбулаев Заур Магомеддибирович", true)
    ) return getParkingAccount(97)

    return null
}
