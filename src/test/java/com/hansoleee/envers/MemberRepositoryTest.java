package com.hansoleee.envers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionSort;
import org.springframework.data.history.Revisions;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @Test
    void basicTest() throws Exception {
        for (int i = 1; i <= 10; i++) {
            em.persist(new Member("member" + i, i));
        }
        em.flush();
        em.clear();

        Member findMember2 = memberRepository.findById(1L).get();
        findMember2.setAge(10);
        em.flush();
        em.clear();

        Member result = memberRepository.findById(1L).get();

        assertThat(result.getAge()).isEqualTo(10);
    }
    @Test
    void changeMember1() throws Exception {
        Member findMember1 = memberRepository.findById(1L).get();
        findMember1.setAge(2222);
        em.flush();
        em.clear();
    }

    @Test
    void test1() throws Exception {
        List<Member> members = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            members.add(memberRepository.findById((long) (i + 1)).get());
        }

        members.forEach(m -> m.setAge(m.getAge() + 100));
        em.flush();
        em.clear();
    }

    @Test
    void test2() throws Exception {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            teams.add(new Team("team" + (char)(65 + i)));
        }
        teamRepository.saveAll(teams);

        List<Member> members = new ArrayList<>();
        for (int i = 5; i < 10; i++) {
            Member member = memberRepository.findById((long) (i + 1)).get();
            member.setAge(member.getAge() + 100);
            member.setTeam(teams.get(i - 5));
        }
        em.flush();
        em.clear();
    }

    @Test
    void 최근_리비전_조회() throws Exception {
        Revision<Integer, Member> integerMemberRevision = memberRepository.findLastChangeRevision(1L).get();
        System.out.println("integerMemberRevision = " + integerMemberRevision);
    }

    @Test
    void 모든_리비전_조회() throws Exception {
        Revisions<Integer, Member> revisions = memberRepository.findRevisions(1L);
        System.out.println("revisions = " + revisions);
    }

    @Test
    void 페이징_정렬조회() throws Exception {
        Page<Revision<Integer, Member>> result = memberRepository.findRevisions(1L,
                PageRequest.of(0, 3, RevisionSort.desc()));

        long totalElements = result.getTotalElements();// 전체 수
        List<Revision<Integer, Member>> content = result.getContent();// 내용

        System.out.println("totalElements = " + totalElements);
        System.out.println("content = " + content);
    }

    @Test
    void 특정_리비전_조회() throws Exception {
        Revision<Integer, Member> integerMemberRevision = memberRepository.findRevision(1L, 1).get();
        integerMemberRevision.getEntity(); // 엔티티
        integerMemberRevision.getRevisionNumber(); // 리비전
        integerMemberRevision.getRevisionInstant(); // 변경 날짜
    }
}