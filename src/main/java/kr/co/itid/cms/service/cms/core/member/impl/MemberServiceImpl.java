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
}