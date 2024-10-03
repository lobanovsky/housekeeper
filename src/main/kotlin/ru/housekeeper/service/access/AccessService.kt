package ru.housekeeper.service.access

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.housekeeper.enums.AccessBlockReasonEnum
import ru.housekeeper.exception.AccessToAreaException
import ru.housekeeper.model.dto.access.*
import ru.housekeeper.model.dto.access.OverviewAccessVO
import ru.housekeeper.model.dto.eldes.EldesContact
import ru.housekeeper.model.dto.toOwnerVO
import ru.housekeeper.model.entity.access.Access
import ru.housekeeper.model.entity.access.AccessToArea
import ru.housekeeper.repository.access.AccessRepository
import ru.housekeeper.service.AreaService
import ru.housekeeper.service.OwnerService
import ru.housekeeper.service.RoomService
import ru.housekeeper.utils.*
import java.time.LocalDateTime

@Service
class AccessService(
    private val accessRepository: AccessRepository,
    private val roomService: RoomService,
    private val carService: CarService,
    private val ownerService: OwnerService,
    private val areaService: AreaService,
) {

    @Transactional
    fun create(
        createAccessRequest: CreateAccessRequest,
        active: Boolean = true
    ): List<CreateAccessResponse> {
        if (createAccessRequest.areas.isEmpty()) throw AccessToAreaException("Не указаны зоны доступа")
        if (createAccessRequest.contacts.isEmpty()) throw AccessToAreaException("Не указаны телефоны")

        val results = mutableListOf<CreateAccessResponse>()

        for (contact in createAccessRequest.contacts) {
            try {
                //create access
                val access = create(
                    ownerIds = createAccessRequest.ownerIds,
                    contact = contact,
                    areas = createAccessRequest.areas,
                    active = active
                )
                //add cars
                access.id?.let { contact.cars?.let { cars -> carService.addCars(it, cars, active) } }
                //add response
                results.add(CreateAccessResponse(access.id, access.phoneNumber.beautifulPhonePrint()))
            } catch (e: AccessToAreaException) {
                logger().error("[$contact] Ошибка: $e")
                results.add(
                    CreateAccessResponse(
                        phoneNumber = contact.number,
                        result = CreateAccessResult(false, e.message)
                    )
                )
            }
        }

        return results
    }

    private fun create(
        ownerIds: Set<Long>,
        contact: Contact,
        areas: Set<AccessToArea>,
        active: Boolean
    ): Access {
        val phoneNumber = contact.number.onlyNumbers()
        phoneNumberValidator(phoneNumber, active)
        val access = Access(
            ownerIds = ownerIds,
            phoneNumber = phoneNumber,
            phoneLabel = contact.label?.trim(),
            active = active
        ).apply {
            this.areas.addAll(areas)
        }
        return accessRepository.save(access)
    }

    @Transactional
    fun update(accessId: Long, accessUpdateRequest: UpdateAccessRequest): AccessVO {
        val areas = accessUpdateRequest.areas
        if (areas.isEmpty()) throw AccessToAreaException("Не указаны зоны доступа")

        val access = accessRepository.findByIdOrNull(accessId)?.let { access ->
            access.phoneLabel = accessUpdateRequest.label?.trim()
            access.areas.clear()
            access.areas.addAll(areas)
            accessRepository.save(access)
        } ?: entityNotfound("Доступ" to accessId)
        //update cars
        carService.updateCars(accessId, accessUpdateRequest.cars ?: setOf())

        return findByOwner(access.ownerIds.first())
    }

    @Transactional
    fun deactivateAccess(
        accessId: Long,
        blockedDateTime: LocalDateTime = LocalDateTime.now(),
        blockReason: AccessBlockReasonEnum = AccessBlockReasonEnum.MANUAL
    ) = findById(accessId).also { access ->
        access.id?.let { deactivateAccessById(it, blockedDateTime, blockReason) }
        access.id?.let { carService.deactivateCar(it) }
    }

    fun findById(id: Long): Access = accessRepository.findByIdOrNull(id) ?: entityNotfound("Доступ" to id)

    fun findByPhone(phoneNumber: String, active: Boolean = true): Access? = accessRepository.findByPhoneNumber(phoneNumber, active)

    private fun deactivateAccessById(accessId: Long, blockedDateTime: LocalDateTime, blockReason: AccessBlockReasonEnum) =
        accessRepository.deactivateById(accessId, blockedDateTime, blockReason)

    fun findByRoom(roomId: Long, active: Boolean): AccessVO? {
        val owners = ownerService.findByRoomId(roomId, active)
        return owners[0].id?.let { findByOwner(it) }
    }

    fun findByPhoneNumber(phoneNumber: String, active: Boolean): AccessVO? {
        val access = findByPhone(phoneNumber, active)
        if (access == null) {
            logger().error("Не найден доступ по номеру [$phoneNumber]")
            return null
        }
        val owner = findById(access.ownerIds.first())
        return owner.id?.let { findByOwner(it, access.id) }
    }

    fun findByCarNumber(carNumber: String, active: Boolean): AccessVO? {
        val cars = carService.findByNumberLike(carNumber, active)
        if (cars.size > 1) throw AccessToAreaException("Найдено несколько автомобилей с номером [$carNumber]. Уточните номер автомобиля")
        val access = findById(cars[0].accessId)
        val owner = ownerService.findById(access.ownerIds.first())
        return owner.id?.let { findByOwner(it, access.id) }
    }

    private fun findByOwner(ownerId: Long, accessId: Long? = null): AccessVO {
        val owner = ownerService.findById(ownerId)
        val accessKeyVOS = accessKeyVOS(accessRepository.findByOwnerId(ownerId))
        val accessVO = AccessVO(
            id = accessId,
            owner = owner.toOwnerVO(roomService.findByIds(owner.rooms)),
            keys = if (accessKeyVOS.isEmpty()) null else accessKeyVOS
        )
        return accessVO
    }

    private fun accessKeyVOS(accesses: List<Access>): List<KeyVO> {
        val existAreas = areaService.findAll().associateBy { it.id }
        return accesses.map { access ->
            KeyVO(
                id = access.id,
                phoneNumber = access.phoneNumber.beautifulPhonePrint(),
                phoneLabel = access.phoneLabel,
                areas = access.areas.map { it.toAreaVO(existAreas[it.areaId]?.name) },
                cars = if (carService.findByAccessId(access.id!!).isEmpty()) null else carService.findByAccessId(access.id).map { it.toCarVO() }
            )
        }
    }

    private fun phoneNumberValidator(phoneNumber: String, active: Boolean) {
        if (phoneNumber.first() != '7') throw AccessToAreaException("Номер телефона [$phoneNumber] должен начинаться с 7")
        if (phoneNumber.length != PHONE_NUMBER_LENGTH) throw AccessToAreaException("Номер телефона [$phoneNumber] должен содержать 11 цифр")
        //check only active phone number
        if (active) {
            accessRepository.findByPhoneNumber(phoneNumber)?.let { access ->
                val owner = ownerService.findById(access.ownerIds.first())
                val rooms = owner.let { it1 -> roomService.findByIds(it1.rooms) }.map { it.toRoomVO() }
                throw AccessToAreaException("Данный номер [${phoneNumber}] уже зарегистрирован. Собственник: ${owner.fullName}: ${rooms.map { it.type.description + " " + it.number }}")
            }
        }
    }

    fun getEldesContact(areadId: Long): List<String> {
        val accesses = accessRepository.findByAreaId(areadId)
        val contacts = mutableListOf<String>()
        contacts.add("User Name;Tel Number;Relay No.;Sch.1 (1-true 0-false);Sch.2 (1-true 0-false);Sch.3 (1-true 0-false);Sch.4 (1-true 0-false);Sch.5 (1-true 0-false);Sch.6 (1-true 0-false);Sch.7 (1-true 0-false);Sch.8 (1-true 0-false);Year (Valid until);Month (Valid until);Day (Valid until);Hour (Valid until);Minute (Valid until);Ring Counter;Ring Counter Status")
        accesses.forEach { access ->
            val owner = ownerService.findById(access.ownerIds.first())
            val firstRoom = roomService.findByIds(owner.rooms).sortedBy { it.type }.first();
            val label = firstRoom.number + "-" + firstRoom.type.name
            contacts.add(
                EldesContact(
                    userName = if (label.length > MAX_ELDES_LABEL_LENGTH) label.substring(0, MAX_ELDES_LABEL_LENGTH) else label,
                    telNumber = access.phoneNumber,
                ).toCSVLine()
            )
        }
        return contacts
    }

    fun getOverview(plateNumber: String, active: Boolean): List<OverviewAccessVO> =
        carService.findByNumberLike(plateNumber, active).map { car ->
            val access = findById(car.accessId)
            val owner = ownerService.findById(access.ownerIds.first())
            OverviewAccessVO(
                ownerName = owner.fullName,
                ownerRooms = roomService.findByIds(owner.rooms).sortedBy { it.type }.joinToString { it.type.shortDescription + "" + it.number },
                phoneNumber = access.phoneNumber.beautifulPhonePrint(),
                phoneLabel = access.phoneLabel,
                carNumber = car.plateNumber,
                carDescription = car.description,
                overviewAreas = access.areas.map { areaToAccess ->
                    OverviewArea(
                        areaName = areaService.findById(areaToAccess.areaId).name,
                        tenant = areaToAccess.tenant,
                        places = areaToAccess.places
                    )
                }
            )
        }

}