package com.back.domain.member.member.repository

import com.back.domain.member.member.entity.Member
import com.back.standard.extentions.getOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberRepositoryTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `findById()`() {
        val member : Member = memberRepository.findById(1).get()

        assertThat(member.id).isEqualTo(1);
    }

    @Test
    fun `findQById()`() {
        val member : Member = memberRepository.findQById(1).getOrThrow()

        assertThat(member.id).isEqualTo(1);
    }

    @Test
    fun `findByUsername()`() {
        val member = memberRepository.findByUsername("user1").getOrThrow()
        assertThat(member.username).isEqualTo("user1")
    }

    @Test
    fun `findQByUsername()`() {
        val member = memberRepository.findQByUserName("user1").getOrThrow()
        assertThat(member.username).isEqualTo("user1")
    }

    @Test
    fun `findByIdIn()`() {
        val memberList = memberRepository.findByIdIn(listOf(1,2,3))
        assertThat(memberList.map { it.id }).containsAnyOf(1,2,3)
    }

    @Test
    fun `findQByIdIn()`() {
        val memberList = memberRepository.findQByIdIn(listOf(1,2,3))
        assertThat(memberList.map { it.id }).containsAnyOf(1,2,3)
    }

    @Test
    fun `findByUsernameAndNickname()`() {
        val member = memberRepository.findByUsernameAndNickname("user1", "유저1").getOrThrow()
        assertThat(member.username).isEqualTo("user1")
        assertThat(member.nickname).isEqualTo("유저1")
    }

    @Test
    fun `findQByUsernameAndNickname()`() {
        val member = memberRepository.findQByUsernameAndNickname("user1", "유저1").getOrThrow()
        assertThat(member.username).isEqualTo("user1")
        assertThat(member.nickname).isEqualTo("유저1")
    }


    @Test
    fun `findByUsernameOrNickname()`() {
        val memberList = memberRepository.findByUsernameOrNickname("user1", "유저2")
        assertThat(memberList.map { it.username }).containsAnyOf("user1", "user2")
    }

    @Test
    fun `findQByUsernameOrNickname()`() {
        val memberList = memberRepository.findQByUsernameOrNickname("user1", "유저2")
        assertThat(memberList.map { it.username }).containsAnyOf("user1", "user2")
    }

    @Test
    fun `findCByUsernameAndEitherPasswordOrNickname`() {
        // select * from member where username = ? and (password = ? or nickname = ?)
        val members = memberRepository.findCByUsernameAndEitherPasswordOrNickname("admin", "wrong-password", "운영자")

        assertThat(members).isNotEmpty
        assertThat(members.any { it.username == "admin" && (it.password == "wrong-password" || it.nickname == "운영자") }).isTrue
    }

    @Test
    fun `findQByUsernameAndPasswordOrNickname`() {
        // select * from member where username = ? and (password = ? or nickname = ?)
        val members = memberRepository.findQByUsernameAndEitherPasswordOrNickname("admin", "wrong-password", "운영자")

        assertThat(members).isNotEmpty
        assertThat(members.any { it.username == "admin" && (it.password == "wrong-password" || it.nickname == "운영자") }).isTrue
    }


    @Test
    fun `findByNicknameContaining`() {
        val members = memberRepository.findByNicknameContaining("유저")

        assertThat(members).isNotEmpty
        assertThat(members.all { it.nickname.contains("유저") }).isTrue
    }

    @Test
    fun `findQByNicknameContaining`() {
        val members = memberRepository.findQByNicknameContaining("유저")

        assertThat(members).isNotEmpty
        assertThat(members.all { it.nickname.contains("유저") }).isTrue
    }

    @Test
    fun `countByNicknameContaining`() {
        val count = memberRepository.countByNicknameContaining("유저")

        assertThat(count).isEqualTo(3)
    }

    @Test
    fun `countQByNicknameContaining`() {
        val count = memberRepository.countQByNicknameContaining("유저")

        assertThat(count).isEqualTo(3)
    }

    @Test
    fun `existsByNicknameContaining`() {
        val exists = memberRepository.existsByNicknameContaining("유저")

        assertThat(exists).isTrue
    }

    @Test
    fun `existsQByNicknameContaining`() {
        val exists = memberRepository.existsQByNicknameContaining("유저")

        assertThat(exists).isTrue
    }

    @Test
    fun `findByNicknameContaining with Pageable`() {
        val pageable = PageRequest.of(0, 2)
        val page = memberRepository.findByNicknameContaining("유저", pageable)

        assertThat(page.content).hasSize(2)
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.totalPages).isEqualTo(2)
    }


    @Test
    fun `findQByNicknameContaining with Pageable`() {
        val pageable = PageRequest.of(0, 2)
        val page = memberRepository.findQByNicknameContaining("유저", pageable).getOrThrow()

        assertThat(page.content).hasSize(2)
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.totalPages).isEqualTo(2)
    }
    @Test
    fun `findByNicknameContainingOrderByIdDesc`() {
        val members = memberRepository.findByNicknameContainingOrderByIdDesc("유저")

        assertThat(members).isNotEmpty
        assertThat(members.all { it.nickname.contains("유저") }).isTrue

        for (i in 0 until members.size - 1) {
            assertThat(members[i].id).isGreaterThan(members[i + 1].id)
        }
    }

    @Test
    fun `findQByNicknameContainingOrderByIdDesc`() {
        val members = memberRepository.findQByNicknameContainingOrderByIdDesc("유저")

        assertThat(members).isNotEmpty
        assertThat(members.all { it.nickname.contains("유저") }).isTrue

        for (i in 0 until members.size - 1) {
            assertThat(members[i].id).isGreaterThan(members[i + 1].id)
        }
    }
}