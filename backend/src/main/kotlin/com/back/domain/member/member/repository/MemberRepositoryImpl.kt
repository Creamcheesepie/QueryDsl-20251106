package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember
import com.back.domain.member.member.entity.QMember.member
import com.back.standard.enum.MemberSearchKeywordType
import com.back.standard.enum.MemberSearchSortType
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

class MemberRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : MemberRepositoryCustom {
    override fun findQById(id: Long): Member? {
        val Member = QMember.member

        return jpaQueryFactory
            .selectFrom(member)
            .where(member.id.eq(id))
            .fetchOne()
    }

    override fun findQByUserName(name: String): Member? {
        val Member = QMember.member

        return jpaQueryFactory
            .selectFrom(member)
            .where(member.username.eq(name))
            .fetchOne()
    }

    override fun findQByUsernameOrNickname(
        username: String,
        nickname: String
    ): List<Member> {
        val Member = QMember.member

        return jpaQueryFactory
            .selectFrom(member)
            .where(member.nickname.eq(nickname).or(member.username.eq(username)))
            .fetch()
    }

    override fun findQByIdIn(ids: List<Long>): List<Member> {
        val Member = QMember.member

        return jpaQueryFactory
            .selectFrom(member)
            .where(member.id.`in`(ids))
            .fetch()
    }

    override fun findQByUsernameAndNickname(
        username: String,
        nickname: String
    ): Member? {
        val Member = QMember.member
        return jpaQueryFactory
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .and(member.nickname.eq(nickname))
            )
            .fetchOne()
    }

    override fun findQByUsernameAndEitherPasswordOrNickname(
        username: String,
        password: String,
        nickname: String
    ): List<Member> {
        val Member = QMember.member

        return jpaQueryFactory
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .and(
                        member.password.eq(password)
                            .or(member.nickname.eq(nickname))
                    )
            )
            .fetch()
    }

    override fun findQByNicknameContaining(nickname: String): List<Member> {
        val Member = QMember.member

        return jpaQueryFactory
            .selectFrom(member)
            .where(
//                member.nickname.contains(nickname)
                member.nickname.like("%${nickname}%") // like 사용시 앞 뒤로 조건을 넣어 줄 %를 넣어주어야 한다.
            )
            .fetch()
    }

    //TODO 이 부분 찝찝해, 나중에 풀어보기.
    override fun countQByNicknameContaining(nickname: String): Long {
        val Member = QMember.member

        return jpaQueryFactory
            .select(
                member.count()
            )
            .from(member)
            .where(member.nickname.contains(nickname))
            .fetchOne() ?: 0L

    }

    override fun existsQByNicknameContaining(nickname: String): Boolean {
        val Member = QMember.member

        return jpaQueryFactory
            .selectOne()
            .from(member)
            .where(member.nickname.contains(nickname))
            .fetchFirst() == 1;
    }

    override fun findQByNicknameContaining(
        nickname: String,
        pageable: Pageable
    ): Page<Member> {
        val Member = QMember.member

        // content 쿼리
        val content = jpaQueryFactory
            .select(member)
            .from(member)
            .where(member.nickname.like("%${nickname}%"))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(content, pageable, {
            jpaQueryFactory
                .select(member.count())
                .from(member)
                .where(member.nickname.like("%${nickname}%"))
                .fetchOne() ?: 0L
        })
    }

    override fun findQByNicknameContainingOrderByIdDesc(
        nickname: String
    ): List<Member> {
        val Member = QMember.member

        return jpaQueryFactory
            .select(member)
            .from(member)
            .where(member.nickname.contains(nickname))
            .orderBy(member.id.desc())
            .fetch()
    }

    override fun findQByUsernameContaining(
        string: String,
        pageable: Pageable
    ): Page<Member> {
        val Member = QMember.member

        val query = jpaQueryFactory
            .select(member)
            .from(member)
            .where(member.username.like("%${string}%"))

        pageable.sort.forEach { order ->
            when (order.property) {
                "id" -> query.orderBy(if (order.isAscending) member.id.asc() else member.id.desc())
                "nickname" -> query.orderBy(if (order.isAscending) member.nickname.asc() else member.nickname.desc())
                "username" -> query.orderBy(if (order.isAscending) member.username.asc() else member.username.desc())
            }
        }

        val content = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(
            content, pageable,
            {
                jpaQueryFactory
                    .select(member.count())
                    .from(member)
                    .where(member.username.like("${String}"))
                    .fetchOne() ?: 0L
            })

    }

    override fun findByKwPaged(
        kw: String,
        kwType: MemberSearchKeywordType,
        pageable: Pageable
    ): Page<Member> {
        val Member = QMember.member

        val builder = BooleanBuilder()?.apply {
            when (kwType) {
                MemberSearchKeywordType.USERNAME -> this.and(member.username.like("%${kw}%"))
                MemberSearchKeywordType.NICKNAME -> this.and(member.nickname.like("%${kw}%"))
                MemberSearchKeywordType.ALL -> this.and(
                    member.username.like("%${kw}%").or(member.nickname.like("%${kw}%"))
                )
            }
        }



        // content 쿼리
        val query = jpaQueryFactory
            .select(member)
            .from(member)
            .where(builder)

        pageable.sort.forEach { order ->
             val path = when (order.property) {
                MemberSearchSortType.ID.property -> member.id
                MemberSearchSortType.NICKNAME.property -> member.nickname
                MemberSearchSortType.USERNAME.property -> member.username
                else -> null
            }

            path?.let { property ->
                OrderSpecifier(
                    if(order.isAscending) Order.ASC else Order.DESC,
                    property,
                )?. let{
                    query.orderBy(it)
                }
            }
        }

        val content = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()



        return PageableExecutionUtils.getPage(
            content, pageable,
            {
                jpaQueryFactory
                    .select(member.count())
                    .from(member)
                    .where(builder)
                    .fetchOne() ?: 0L
            })
    }
}