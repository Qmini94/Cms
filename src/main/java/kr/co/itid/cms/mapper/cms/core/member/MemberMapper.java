package kr.co.itid.cms.mapper.cms.core.member;

import kr.co.itid.cms.dto.cms.core.member.request.MemberCreateRequest;
import kr.co.itid.cms.dto.cms.core.member.request.MemberUpdateRequest;
import kr.co.itid.cms.dto.cms.core.member.response.MemberListResponse;
import kr.co.itid.cms.dto.cms.core.member.response.MemberResponse;
import kr.co.itid.cms.entity.cms.core.member.Member;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    /** 생성: userId, userName만 매핑 (비밀번호는 서비스에서 설정) */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "userName", source = "userName")
    Member toEntity(MemberCreateRequest request);

    /** 수정: null 값은 무시하고 부분 업데이트만 반영 */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Member entity, MemberUpdateRequest request);

    /** 목록 응답 매핑: regDate -> createdDate */
    @Mapping(source = "regDate", target = "createdDate")
    MemberListResponse toListResponse(Member entity);

    /** 단건 응답 매핑: regDate -> createdDate */
    @Mapping(source = "regDate", target = "createdDate")
    MemberResponse toResponse(Member entity);
}