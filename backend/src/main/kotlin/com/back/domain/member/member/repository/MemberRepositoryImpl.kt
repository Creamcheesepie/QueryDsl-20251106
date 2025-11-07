package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember
import com.back.domain.member.member.entity.QMember.member
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

        // totalCount 쿼리
        val totalCount= jpaQueryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.like("%${nickname}%"))
            .fetchOne() ?: 0L



        return PageableExecutionUtils.getPage(content,pageable,{
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
}