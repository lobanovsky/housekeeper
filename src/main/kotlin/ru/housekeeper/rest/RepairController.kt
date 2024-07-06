package ru.housekeeper.rest

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.housekeeper.service.RepairService

@CrossOrigin
@RestController
@RequestMapping("/repairs")
class RepairController(
    private val repairService: RepairService,
) {

    @PutMapping("/remove-duplicates")
    fun removeDuplicates() {
        repairService.findAndRemoveDuplicates()
    }

    @PutMapping("/update-uuid")
    fun updateUUID() {
        repairService.updateUUID()
    }
}