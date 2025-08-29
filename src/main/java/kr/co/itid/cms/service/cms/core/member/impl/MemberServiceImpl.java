package kr.co.itid.cms.service.cms.core.member.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.member.request.MemberCreateRequest;
import kr.co.itid.cms.dto.cms.core.member.request.MemberUpdateRequest;
import kr.co.itid.cms.dto.cms.core.member.response.MemberListResponse;
import kr.co.itid.cms.dto.cms.core.member.response.MemberResponse;
import kr.co.itid.cms.entity.cms.core.member.Member;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.member.MemberMapper;
import kr.co.itid.cms.repository.cms.core.member.MemberRepository;
import kr.co.itid.cms.service.cms.core.member.MemberService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import kr.co.itid.cms.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service("memberService")
@RequiredArgsConstructor
public class MemberServiceImpl extends EgovAbstractServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final LoggingUtil loggingUtil;
    private final ValidationUtil validationUtil;
    private final PasswordEncoder passwordEncoder;

    /** 목록: 검색 + 페이징 */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Page<MemberListResponse> searchMembers(SearchOption option, Pageable pageable) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to search member list");
        try {
            Page<Member> page = memberRepository.searchByCondition(option, pageable);
            loggingUtil.logSuccess(Action.RETRIEVE, "Member list retrieved (total=" + page.getTotalElements() + ")");
            return page.map(memberMapper::toListResponse);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("회원 목록 조회 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("회원 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /** 단건 조회 */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public MemberResponse getMemberByIdx(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "회원 단건 조회 시도: idx=" + idx);
        try {
            Member entity = memberRepository.findById(idx)
                    .orElseThrow(() -> new EgovBizException("존재하지 않는 회원입니다. idx=" + idx));
            MemberResponse dto = memberMapper.toResponse(entity);
            loggingUtil.logSuccess(Action.RETRIEVE, "회원 조회 성공: idx=" + idx);
            return dto;
        } catch (EgovBizException e) {
            loggingUtil.logFail(Action.RETRIEVE, e.getMessage());
            throw e;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "회원 조회 실패: " + e.getMessage());
            throw processException("회원 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Map<String, String> getDisplayNamesByIds(Set<String> idxList) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "배치 이름 조회 시도: count=" + (idxList == null ? 0 : idxList.size()));
        try {
            if (idxList == null || idxList.isEmpty()) {
                return java.util.Collections.emptyMap();
            }

            // 문자열 ID → Long (숫자만)
            java.util.LinkedHashSet<Long> ids = new java.util.LinkedHashSet<>();
            for (String s : idxList) {
                if (s == null) continue;
                String t = s.trim();
                if (t.isEmpty()) continue;
                if (t.matches("\\d+")) {
                    ids.add(Long.parseLong(t));
                }
            }
            if (ids.isEmpty()) {
                return java.util.Collections.emptyMap();
            }

            // 배치 조회
            java.util.List<Member> members = memberRepository.findAllById(ids);

            // 결과: key="idx"(문자열), value=userName
            java.util.LinkedHashMap<String, String> out =
                    new java.util.LinkedHashMap<>(members.size());
            for (Member m : members) {
                out.put(String.valueOf(m.getIdx()), m.getUserName());
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "배치 이름 조회 성공: found=" + out.size());
            return out;

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("회원 이름 배치 조회 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("회원 이름 배치 조회 중 오류가 발생했습니다.", e);
        }
    }

    /** 생성: userId = 평문비번 → encoder로 암호화 저장 */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void createMemberWithIdEqualsPassword(MemberCreateRequest request) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        validationUtil.validateBadWords(request.getUserName(), user);

        final String userId = request.getUserId();
        loggingUtil.logAttempt(Action.CREATE, "회원 생성 시도: userId=" + userId);

        try {
            if (memberRepository.existsByUserId(userId)) {
                throw new EgovBizException("이미 사용 중인 아이디입니다.");
            }

            // mapper로 기본 필드 매핑
            Member entity = memberMapper.toEntity(request);

            // 비밀번호 암호화
            entity.setUserPassword(passwordEncoder.encode(userId));
            entity.setUserPin(userId);

            entity.setRegDate(LocalDateTime.now());

            // 기본값 정책(필요 시)
            if (entity.getUserLevel() == null) entity.setUserLevel(9); // 예: 기본 USER

            memberRepository.save(entity);
            loggingUtil.logSuccess(Action.CREATE, "회원 생성 완료: userId=" + userId);
        } catch (EgovBizException e) {
            loggingUtil.logFail(Action.CREATE, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.CREATE, "DB error: " + e.getMessage());
            throw processException("회원 생성 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "회원 생성 실패: " + e.getMessage());
            throw processException("회원 생성 중 오류가 발생했습니다.", e);
        }
    }

    /** 수정: mapper의 null-ignore 업데이트 사용 (userId/비번 변경 없음) */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void updateMember(Long idx, MemberUpdateRequest request) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            validationUtil.validateBadWords(request.getUserName(), user);
        }

        loggingUtil.logAttempt(Action.UPDATE, "회원 수정 시도: idx=" + idx);
        try {
            Member entity = memberRepository.findById(idx)
                    .orElseThrow(() -> new EgovBizException("존재하지 않는 회원입니다. idx=" + idx));

            // 부분 업데이트(NullValue IGNORE)
            memberMapper.updateEntity(entity, request);

            // 안전장치: userId/비밀번호는 요구사항상 변경 금지
            // (DTO에 필드가 없어서 변경될 일은 없지만 혹시 몰라 재보정)
            // entity.setUserId(entity.getUserId());
            // entity.setUserPassword(entity.getUserPassword());

            memberRepository.save(entity);
            loggingUtil.logSuccess(Action.UPDATE, "회원 수정 완료: idx=" + idx);
        } catch (EgovBizException e) {
            loggingUtil.logFail(Action.UPDATE, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error: " + e.getMessage());
            throw processException("회원 수정 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "회원 수정 실패: " + e.getMessage());
            throw processException("회원 수정 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<MemberListResponse> searchMembersForSuggest(String keyword, int size) throws Exception {
        final String q = (keyword == null) ? "" : keyword.trim();
        loggingUtil.logAttempt(Action.RETRIEVE, "회원 자동완성 검색 시도: q='" + q + "', size=" + size);

        if (q.isEmpty()) {
            loggingUtil.logSuccess(Action.RETRIEVE, "빈 검색어 → 빈 결과 반환");
            return Collections.emptyList();
        }

        // 상한 보정 (과도한 조회 방지)
        final int limit = Math.max(1, Math.min(size, 20));

        try {
            // 레포지토리 커스텀 메서드(다음 단계에서 구현 예정)
            // - 이름/아이디 LIKE 검색
            // - 상위 limit개만 반환
            List<Member> list = memberRepository.searchForSuggest(q, limit);

            List<MemberListResponse> out = new ArrayList<>(list.size());
            for (Member m : list) {
                out.add(memberMapper.toListResponse(m));
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "자동완성 검색 성공: found=" + out.size());
            return out;

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("회원 자동완성 검색 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("회원 자동완성 검색 중 오류가 발생했습니다.", e);
        }
    }
}
