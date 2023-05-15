package ru.housekeeper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HousekeeperApplication

fun main(args: Array<String>) {
    runApplication<ru.housekeeper.HousekeeperApplication>(*args)
}
