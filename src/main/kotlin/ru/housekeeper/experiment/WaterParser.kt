package ru.housekeeper.experiment

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * Парсер для воды
 */
fun main6() {
    val year = "2026"
    val month = "07"
    process(
        //(папка месяц) Показания, которые сняли со счётчиков
        currentFilename = "/Users/evgeny/Yandex.Disk.localized/Домовладелец/${year}-${month}/counters/${year}-${month}-вода.xlsx",
        //(папка 0-counters) Предыдущие показания, файл нужно пребразовать в .xlsx из .numbers
        previousFilename = "/Users/evgeny/Yandex.Disk.localized/Домовладелец/0-counters/Cчётчики.xlsx",
        //Куда сохранить результат
        destinationPath = "/Users/evgeny/Yandex.Disk.localized/Домовладелец/0-counters/",
        //(указать номер столбца с предыдущими показаниями) Чтобы задать новое значение, нужно прибавить +3
        //94 номер столбца - апрель 2026
        //97 номер столбца - май 2026
        previousColumnValueNumber = 100
    )
}

fun process(
    currentFilename: String,
    previousFilename: String,
    destinationPath: String,
    previousColumnValueNumber: Int
) {
    val values = readCurrentValuesMyFormat(currentFilename)

    val workbook = WorkbookFactory.create(File(previousFilename))
    println("-- read hot")
    val hot = readOldValues(workbook, "Горячая", previousColumnValueNumber, values)
    println("-- read cold")
    val cold = readOldValues(workbook, "Холодная", previousColumnValueNumber, values)

    write(hot, cold, destinationPath)
}

fun readOldValues(workbook: Workbook, sheetName: String, previousColumnValueNumber: Int, values: Map<String, BigDecimal>): List<Result> {
    val sheet = workbook.getSheet(sheetName)
    val result = mutableListOf<Result>()
    for (i in 1..sheet.lastRowNum) {
        val row = sheet.getRow(i)
        val flatCell = row.getCell(0)
        val numberCell = row.getCell(1)
        val valueCell = row.getCell(previousColumnValueNumber)

        val flat = flatCell.stringCellValue
        val number = if (numberCell.cellType == CellType.STRING) numberCell.stringCellValue.trim() else ""
        val oldValue = when (valueCell.cellType) {
            CellType.NUMERIC -> numericToBigDecimal(valueCell)
            CellType.FORMULA -> when (valueCell.cachedFormulaResultType) {
                CellType.NUMERIC -> numericToBigDecimal(valueCell)
                CellType.STRING -> {
                    val value = valueCell.stringCellValue
                    if (value.isBlank()) BigDecimal.ZERO else BigDecimal(value.replace(",", "."))
                }

                else -> BigDecimal.ZERO
            }
            CellType.STRING -> {
                val value = valueCell.stringCellValue
                if (value.isBlank()) BigDecimal.ZERO else BigDecimal(value.replace(",", "."))
            }

            else -> {
                println("Error: $flat $number")
                continue
            }
        }

        val customValue = filterByCustomRules(number, oldValue, values[number] ?: BigDecimal.ZERO)
        result.add(
            Result(
                flat = flat,
                counterNumber = number,
                value = oldValue,
                newValue = customValue.newValue.setScale(2, RoundingMode.HALF_EVEN),
                customDescription = customValue.description
            )
        )
    }
    return result
}

private fun filterByCustomRules(number: String, oldValue: BigDecimal, newValue: BigDecimal): CustomValue {
    val customValue = CustomValue(newValue)

    if (newValue < oldValue) zero(customValue, oldValue, newValue)

    //горячая вода
    //оф.3
    if (number == "875668") plus(customValue, oldValue, newValue, BigDecimal(4.95))
    //оф.5
    if (number == "55464842") plus(customValue, oldValue, newValue, BigDecimal(2.5))
    //2
    if (number == "875745") plus(customValue, oldValue, newValue, BigDecimal(1.86))
    //3
    if (number == "875697") plus(customValue, oldValue, newValue, BigDecimal(1.53))
    //4
    if (number == "875679") plus(customValue, oldValue, newValue, BigDecimal(0.87))
    //5
    if (number == "875778") plus(customValue, oldValue, newValue, BigDecimal(0.43))
    //48
    if (number == "210157253") plus(customValue, oldValue, newValue, BigDecimal(4))
    //49-1
    if (number == "882039") plus(customValue, oldValue, newValue, BigDecimal(4))
    //49-2
    if (number == "876144") plus(customValue, oldValue, newValue, BigDecimal(2))
    //54-1
    if (number == "875900") plus(customValue, oldValue, newValue, BigDecimal(1))
    //54-2
    if (number == "914002") plus(customValue, oldValue, newValue, BigDecimal(2))
    //74
    if (number == "22-473400") plus(customValue, oldValue, newValue, BigDecimal(12))
    //83
    if (number == "220528693") plus(customValue, oldValue, newValue, BigDecimal(5))
    //92
    if (number == "770272") plus(customValue, oldValue, newValue, BigDecimal(2))
    //93
    if (number == "770280") plus(customValue, oldValue, newValue, BigDecimal(0.8))
    //94
    if (number == "770201") plus(customValue, oldValue, newValue, BigDecimal(5))
    //95
    if (number == "816544") plus(customValue, oldValue, newValue, BigDecimal(11))
    //97
    if (number == "190147896") plus(customValue, oldValue, newValue, BigDecimal(16))
    //110
    if (number == "816575") plus(customValue, oldValue, newValue, BigDecimal(0.5))
    //111
    if (number == "770279") plus(customValue, oldValue, newValue, BigDecimal(2))
    //112
    if (number == "770268") plus(customValue, oldValue, newValue, BigDecimal(10))
    //113
    if (number == "770298") plus(customValue, oldValue, newValue, BigDecimal(2))
    //117
    if (number == "443140") plus(customValue, oldValue, newValue, BigDecimal(2.2))
    //118
    if (number == "770488") plus(customValue, oldValue, newValue, BigDecimal(2))
    //122
    if (number == "884821") plus(customValue, oldValue, newValue, BigDecimal(3))
    //123
    if (number == "885071") plus(customValue, oldValue, newValue, BigDecimal(4))
    //124
    if (number == "885072") plus(customValue, oldValue, newValue, BigDecimal(0.5))
    //125
    if (number == "884789") plus(customValue, oldValue, newValue, BigDecimal(8))
    //135-1
    if (number == "885042") plus(customValue, oldValue, newValue, BigDecimal(0.5))

    //Оф.8
    if (number == "875755") plus(customValue, oldValue, newValue, BigDecimal(1))
    //8
    if (number == "689574") {
        plus(customValue, oldValue, newValue, BigDecimal(3))
    }
    //9
    if (number == "689402") plus(customValue, oldValue, newValue, BigDecimal(11))
    //10
    if (number == "689579") plus(customValue, oldValue, newValue, BigDecimal(3))
    //11
    if (number == "686187") plus(customValue, oldValue, newValue, BigDecimal(10))
    //18-1
    if (number == "876011") plus(customValue, oldValue, newValue, BigDecimal(10))
    //18-2
    if (number == "875741") plus(customValue, oldValue, newValue, BigDecimal(0))
    //50
    if (number == "779682") plus(customValue, oldValue, newValue, BigDecimal(1.5))
    //51
    if (number == "775190") plus(customValue, oldValue, newValue, BigDecimal(3.5))
    //52
    if (number == "779601") plus(customValue, oldValue, newValue, BigDecimal(4.5))
    //56
    if (number == "779686") plus(customValue, oldValue, newValue, BigDecimal(2.5))
    //57
    if (number == "770457") plus(customValue, oldValue, newValue, BigDecimal(7))
    //59
    if (number == "779606") plus(customValue, oldValue, newValue, BigDecimal(3.5))
    //60-1
    if (number == "113280") plus(customValue, oldValue, newValue, BigDecimal(1))
    //60-2
    if (number == "113173") plus(customValue, oldValue, newValue, BigDecimal(4))
    //75
    if (number == "884917") plus(customValue, oldValue, newValue, BigDecimal(1.5))
    //76
    if (number == "885077") plus(customValue, oldValue, newValue, BigDecimal(2))
    //77
    if (number == "885073") plus(customValue, oldValue, newValue, BigDecimal(8.5))
    //80
    if (number == "884996") plus(customValue, oldValue, newValue, BigDecimal(3))
    //81
    if (number == "884913") plus(customValue, oldValue, newValue, BigDecimal(3))
    //82
    if (number == "884831") plus(customValue, oldValue, newValue, BigDecimal(4))
    //85-1
    if (number == "779757") plus(customValue, oldValue, newValue, BigDecimal(5.5))
    //85-2
    if (number == "775118") plus(customValue, oldValue, newValue, BigDecimal(9.5))
    //90-1
    if (number == "779769") plus(customValue, oldValue, newValue, BigDecimal(0.5))
    //90-2
    if (number == "779701") plus(customValue, oldValue, newValue, BigDecimal(3))
    //106
    if (number == "816518") plus(customValue, oldValue, newValue, BigDecimal(0.1))
    //116
    if (number == "770277") plus(customValue, oldValue, newValue, BigDecimal(12))
    //119
    if (number == "770264") plus(customValue, oldValue, newValue, BigDecimal(2.5))
    //120-1
    if (number == "779762") plus(customValue, oldValue, newValue, BigDecimal(0.3))
    //120-2
    if (number == "779775") plus(customValue, oldValue, newValue, BigDecimal(2.5))
    //126-1
    if (number == "875864") plus(customValue, oldValue, newValue, BigDecimal(10))
    //134
    if (number == "884914") plus(customValue, oldValue, newValue, BigDecimal(3.5))
    //137
    if (number == "885050") plus(customValue, oldValue, newValue, BigDecimal(14))


    //холодная вода
    //БКФН Гараж
    if (number == "145558") plus(customValue, oldValue, newValue, BigDecimal(18.32))
    //Оф.3
    if (number == "864816") plus(customValue, oldValue, newValue, BigDecimal(13.26))
    //2
    if (number == "864058") plus(customValue, oldValue, newValue, BigDecimal(2.99))
    //3
    if (number == "863983") plus(customValue, oldValue, newValue, BigDecimal(1.69))
    //4
    if (number == "863973") plus(customValue, oldValue, newValue, BigDecimal(3.45))
    //5
    if (number == "864052") plus(customValue, oldValue, newValue, BigDecimal(1.09))
    //21
    if (number == "857841") plus(customValue, oldValue, newValue, BigDecimal(9.5))
    //24
    if (number == "853239") plus(customValue, oldValue, newValue, BigDecimal(2.5))
    //39
    if (number == "866997") plus(customValue, oldValue, newValue, BigDecimal(4))
    //40
    if (number == "210258335") plus(customValue, oldValue, newValue, BigDecimal(3))
    //67
    if (number == "852198") plus(customValue, oldValue, newValue, BigDecimal(10))
    //74
    if (number == "22-519578") plus(customValue, oldValue, newValue, BigDecimal(8.9))
    //83
    if (number == "220671961") plus(customValue, oldValue, newValue, BigDecimal(4))
    //92
    if (number == "853201") plus(customValue, oldValue, newValue, BigDecimal(3))
    //95
    if (number == "852222") plus(customValue, oldValue, newValue, BigDecimal(14))
    //97-2
    if (number == "180622221") plus(customValue, oldValue, newValue, BigDecimal(20))
    //105
    if (number == "852937") plus(customValue, oldValue, newValue, BigDecimal(3))
    //110
    if (number == "848174") plus(customValue, oldValue, newValue, BigDecimal(5))
    //111
    if (number == "857834") plus(customValue, oldValue, newValue, BigDecimal(0))
    //112
    if (number == "857821") plus(customValue, oldValue, newValue, BigDecimal(13))
    //113
    if (number == "857832") plus(customValue, oldValue, newValue, BigDecimal(10))
    //118
    if (number == "50081663") plus(customValue, oldValue, newValue, BigDecimal(4.5))
    //119 - старый
    //if (number == "848177") plus(customValue, oldValue, newValue, BigDecimal(10.0))
    //119 - новый
    if (number == "26-224123") plus(customValue, oldValue, newValue, BigDecimal(1.0))
    //122
    if (number == "848171") plus(customValue, oldValue, newValue, BigDecimal(4))
    //124
    if (number == "852917") plus(customValue, oldValue, newValue, BigDecimal(8))
    //125
    if (number == "848178") plus(customValue, oldValue, newValue, BigDecimal(8))
    //134
    if (number == "853238") plus(customValue, oldValue, newValue, BigDecimal(3.4))
    //139-1
    if (number == "853209") plus(customValue, oldValue, newValue, BigDecimal(1.24))
    //139-2
    if (number == "152086") plus(customValue, oldValue, newValue, BigDecimal(3.86))
    //86 (горячая, заменили счётчик)
    if (number == "107512") plus(customValue, oldValue, newValue, BigDecimal(3.5))
    //Консьерж под.1
    if (number == "864041") plus(customValue, oldValue, newValue, BigDecimal(7.81))

    //8
    if (number == "672306") plus(customValue, oldValue, newValue, BigDecimal(6))
    //9
    if (number == "667878") plus(customValue, oldValue, newValue, BigDecimal(11))
    //10
    if (number == "665306") plus(customValue, oldValue, newValue, BigDecimal(4))
    //11
    if (number == "672310") plus(customValue, oldValue, newValue, BigDecimal(11))
    //13-1
    if (number == "864017") plus(customValue, oldValue, newValue, BigDecimal(0.5))
    //13-2
    if (number == "863944") plus(customValue, oldValue, newValue, BigDecimal(10))
    //18-1
    if (number == "865290") plus(customValue, oldValue, newValue, BigDecimal(19))
    //18-2
    if (number == "863963") plus(customValue, oldValue, newValue, BigDecimal(0.15))
    //50
    if (number == "857867") plus(customValue, oldValue, newValue, BigDecimal(2))
    //51
    if (number == "857863") plus(customValue, oldValue, newValue, BigDecimal(4.5))
    //52
    if (number == "853034") plus(customValue, oldValue, newValue, BigDecimal(10))
    //53
    if (number == "851481") plus(customValue, oldValue, newValue, BigDecimal(3))
    //56
    if (number == "857854") plus(customValue, oldValue, newValue, BigDecimal(7))
    //57
    if (number == "851456") plus(customValue, oldValue, newValue, BigDecimal(7))
    //59
    if (number == "857879") plus(customValue, oldValue, newValue, BigDecimal(8))
    //60-1
    if (number == "122191") plus(customValue, oldValue, newValue, BigDecimal(3))
    //60-2
    if (number == "115264") plus(customValue, oldValue, newValue, BigDecimal(2))
    //75
    if (number == "852081") plus(customValue, oldValue, newValue, BigDecimal(1))
    //76
    if (number == "852091") plus(customValue, oldValue, newValue, BigDecimal(2))
    //77
    if (number == "853144") plus(customValue, oldValue, newValue, BigDecimal(7))
    //80
    if (number == "852092") plus(customValue, oldValue, newValue, BigDecimal(3))
    //81
    if (number == "853205") plus(customValue, oldValue, newValue, BigDecimal(4))
    //85-1
    if (number == "864879") plus(customValue, oldValue, newValue, BigDecimal(0))
    //85-2
    if (number == "864968") plus(customValue, oldValue, newValue, BigDecimal(18))
    //90-1
    if (number == "864969") plus(customValue, oldValue, newValue, BigDecimal(1.5))
    //90-2
    if (number == "864820") plus(customValue, oldValue, newValue, BigDecimal(2))
    //115-1
    if (number == "864819") plus(customValue, oldValue, newValue, BigDecimal(1.2))
    //115-2
    if (number == "862653") plus(customValue, oldValue, newValue, BigDecimal(3))
    //116
    if (number == "852911") plus(customValue, oldValue, newValue, BigDecimal(12))
    //117
    if (number == "857823") plus(customValue, oldValue, newValue, BigDecimal(5))
    //120-1
    if (number == "862651") plus(customValue, oldValue, newValue, BigDecimal(1.3))
    //120-2
    if (number == "864815") plus(customValue, oldValue, newValue, BigDecimal(4))
    //135
    if (number == "857831") plus(customValue, oldValue, newValue, BigDecimal(3))
    //136
    if (number == "844785") plus(customValue, oldValue, newValue, BigDecimal(7))
    //137
    if (number == "853096") plus(customValue, oldValue, newValue, BigDecimal(14))


    //13, 28, 53, 96-1, 115-2, 127-2, 132-1, 136
    //office5, 20, 26, 28, 29, 47, 49-1, 49-2, 82, 93, 94, 123, 127-1, 127-2, 130, 132,
    //131, 86, 13, 65 (х+г), 86, 102, 115, 143
    socialNorm(
        number,
        setOf(
            "875607",
            "200296045",
            "779685",
            "775104",
            "816698",
            "853236",
            "853281",
            "875865",
            "884838",
            "875912",
            "55582120",
            "853089",
            "853087",
            "200465360",
            "853087-1",
            "868981",
            "852083",
            "853143",
            "852224",
            "844791",
            "853208",
            "852090",
            "848175",
            "852237",
            "884916",
            "852902",
            "770211",
            "875688",
            "853026",
            "775416",
            "770211",
            "770262",
            "864962",
            "862656",
            "775126",
            "884769",
            "779608",
            "844798",
            "852110",
            "875644",
            "879991",
            "857855",
            "853230",
            "853267",
            "853164",
            "852925",
            "770270",
            "857834",
            "770279",
            "863973",
            "875679",
            "864052",
            "875778",
            "844794",
            "853231",
        ),
        customValue,
        oldValue,
        newValue
    )

    return customValue
}

private fun zero(customValue: CustomValue, oldValue: BigDecimal, newValue: BigDecimal): CustomValue {
    customValue.newValue = oldValue
    customValue.description = "${newValue.setScale(2, RoundingMode.HALF_EVEN)} -> Старое значение"
    return customValue
}

private fun plus(
    customValue: CustomValue,
    oldValue: BigDecimal,
    newValue: BigDecimal,
    constantValue: BigDecimal,
    description: String? = null,
): CustomValue {
    customValue.newValue = oldValue.plus(constantValue)
    customValue.description =
        description ?: "+${constantValue.setScale(2, RoundingMode.HALF_EVEN)}"
    return customValue
}

private fun socialNorm(
    number: String,
    numbers: Set<String>,
    customValue: CustomValue,
    oldValue: BigDecimal,
    newValue: BigDecimal
): CustomValue {
    if (numbers.contains(number)) {
        val socialNorm = BigDecimal(6.935)
        customValue.newValue = oldValue.plus(socialNorm)
        customValue.description = "соц.норма +${socialNorm.setScale(3, RoundingMode.HALF_EVEN)}"
    }
    return customValue
}

data class CustomValue(
    var newValue: BigDecimal,
    var description: String? = null,
)

fun write(
    hot: List<Result>,
    cold: List<Result>,
    destinationPath: String,
) {
    val fileName = "result"
    val workBook = XSSFWorkbook()
    val hotSheet = workBook.createSheet("Горячая")
    val coldSheet = workBook.createSheet("Холодная")
    writeToSheet(hot, hotSheet)
    writeToSheet(cold, coldSheet)
    workBook.write(File("$destinationPath${fileName}_${LocalDate.now()}.xlsx").outputStream())
    workBook.close()
}

fun writeToSheet(result: List<Result>, sheet: Sheet) {
    var i = 0
    for (result in result) {
        val row = sheet.createRow(i)
        row.createCell(0).setCellValue(result.flat)
        row.createCell(1).setCellValue(result.counterNumber)
        row.createCell(2).setCellValue(result.value.toString().replace(".", ","))
        row.createCell(3).setCellValue(result.newValue.toString().replace(".", ","))
        row.createCell(4).setCellValue(result.customDescription)
        i++
    }
}

fun readCurrentValuesMyFormat(currentValuesFile: String): Map<String, BigDecimal> {
    val workbook = WorkbookFactory.create(File(currentValuesFile))
    val sheet = workbook.getSheetAt(0)

    val newValues = mutableMapOf<String, BigDecimal>()

    for (i in 0..sheet.lastRowNum) {
        val row = sheet.getRow(i)
        if (row == null) continue
        val typeAndCounterCell = row.getCell(1)
        val valueCell = row.getCell(2)

        if (valueCell == null) continue
        if ((typeAndCounterCell.cellType == CellType.STRING || typeAndCounterCell.cellType == CellType.BLANK) && typeAndCounterCell.stringCellValue.isNullOrBlank()) continue
        val typeAndCounterValue = typeAndCounterCell.stringCellValue
        //split by ,
        val values = typeAndCounterValue.split(",")
        if (values.size != 2) continue
        val type = when {
            values[0].trim().startsWith("СГИ") -> "Горячая"
            values[0].trim().startsWith("СХИ") -> "Холодная"
            else -> continue
        }
        val number = values[1].trim().substring(1)
        val value = BigDecimal(valueCell.numericCellValue).setScale(2, RoundingMode.HALF_EVEN)
        println("$type - $number - $value")
        newValues[number] = value
    }
    return newValues
}


private fun numericToBigDecimal(cell: Cell) = BigDecimal(cell.numericCellValue).setScale(2, RoundingMode.HALF_EVEN)

data class Result(
    val flat: String,
    val counterNumber: String,
    val value: BigDecimal,
    val newValue: BigDecimal?,
    val customDescription: String?,
)