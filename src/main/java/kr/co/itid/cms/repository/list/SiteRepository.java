package kr.co.itid.cms.repository.list;

import kr.co.itid.cms.entity.cms.list.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
}
