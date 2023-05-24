package ru.housekeeper.repository

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
    pageSize: Int
): Page<T> {
    val query = entityManager.createQuery(sql, T::class.java)
    val pageable: Pageable = PageRequest.of(pageNum, pageSize)
    query.firstResult = pageSize * pageNum
    query.maxResults = pageable.pageSize
    return PageableExecutionUtils.getPage(
        query.resultList,
        pageable
    ) { entityManager.createQuery(sqlCount).resultList[0] as Long }
}