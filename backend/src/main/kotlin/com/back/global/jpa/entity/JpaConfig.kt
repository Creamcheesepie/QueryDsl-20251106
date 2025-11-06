package com.back.global.jpa.entity

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JpaConfig(
    private val entityManager: EntityManager
) {

    @Bean
    fun JpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}