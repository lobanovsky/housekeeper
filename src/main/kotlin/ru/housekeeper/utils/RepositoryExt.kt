package ru.housekeeper.utils

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

inline fun <reified T> getPage(
    entityManager: EntityManager,
    sql: String,
    sqlCount: String,
    pageNum: Int,
    pageSize: Int,
    useNativeQuery: Boolean = false
): Page<T> {
    val query = if (!useNativeQuery) {
        entityManager.createQuery(sql, T::class.java)
    } else {
        entityManager.createNativeQuery(sql, T::class.java)
    }

    val pageable: Pageable = PageRequest.of(pageNum, pageSize)
    query.firstResult = pageSize * pageNum
    query.maxResults = pageable.pageSize
    return PageableExecutionUtils.getPage(
        query.resultList as List<T>,
        pageable
    ) {
        if (!useNativeQuery) {
            entityManager.createQuery(sqlCount).resultList[0]
        } else {
            entityManager.createNativeQuery(sqlCount).resultList[0]
        } as Long
    }
}