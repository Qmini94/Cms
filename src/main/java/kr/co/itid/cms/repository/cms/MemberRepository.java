package kr.co.itid.cms.repository.cms;

import kr.co.itid.cms.entity.cms.base.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByUserId(String userId);
}
