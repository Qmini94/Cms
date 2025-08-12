package kr.co.itid.cms.repository.cms.core.member;

import kr.co.itid.cms.entity.cms.core.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    boolean existsByUserId(String userId);
    Optional<Member> findByUserId(String userId);
}