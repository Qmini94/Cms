package kr.co.itid.cms.repository.cms.core.content;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.entity.cms.core.content.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentRepositoryCustom {
    Page<Content> searchByCondition(SearchOption option, Pageable pageable);
}
