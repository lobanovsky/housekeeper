package ru.housekeeper.excel

import ru.housekeeper.model.entity.Decision
import java.math.BigDecimal

class QuestionResult(
    private var number: Int,
    //percentage
    var percentOfYes: BigDecimal = BigDecimal.ZERO,
    var percentOfNo: BigDecimal = BigDecimal.ZERO,
    var percentOfUndefined: BigDecimal = BigDecimal.ZERO,
    //count
    private var numberOfYes: Int = 0,
    private var numberOfNo: Int = 0,
    private var numberOfUndefined: Int = 0,
    //decisions
    private var decisionsOfYes: MutableList<Decision> = mutableListOf(),
    private var decisionsOfNo: MutableList<Decision> = mutableListOf(),
    private var decisionsOfUndefined: MutableList<Decision> = mutableListOf()
) {
    fun incYes(percent: BigDecimal, decision: Decision) {
        percentOfYes = percentOfYes.add(percent)
        numberOfYes++
        decisionsOfYes.add(decision)
    }

    fun incNo(percent: BigDecimal, decision: Decision) {
        percentOfNo = percentOfNo.add(percent)
        numberOfNo++
        decisionsOfNo.add(decision)
    }

    fun incUndefined(percent: BigDecimal, decision: Decision) {
        percentOfUndefined = percentOfUndefined.add(percent)
        numberOfUndefined++
        decisionsOfUndefined.add(decision)
    }
}