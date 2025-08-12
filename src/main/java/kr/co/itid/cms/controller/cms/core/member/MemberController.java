package kr.co.itid.cms.controller.cms.core.member;

import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.member.request.MemberCreateRequest;
import kr.co.itid.cms.dto.cms.core.member.request.MemberUpdateRequest;
import kr.co.itid.cms.dto.cms.core.member.response.MemberListResponse;
import kr.co.itid.cms.dto.cms.core.member.response.MemberResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/member")
@Validated
public class MemberController {

    private final MemberService memberService;

    // 목록(검색 + 페이징)
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberListResponse>>> getMembers(
            @Valid @ModelAttribute SearchOption option,
            @Valid @ModelAttribute PaginationOption pagination,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            // 요청 바인딩 오류는 400으로 응답
            return ResponseEntity.badRequest().body(ApiResponse.error(400, errorMsg));
        }

        Pageable pageable = pagination.toPageable();
        Page<MemberListResponse> page = memberService.searchMembers(option, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    // 단건 조회
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMember(
            @PathVariable @Positive(message = "회원 IDX는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        MemberResponse dto = memberService.getMemberByIdx(idx);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // 생성: userId, userName만 수신. 비밀번호는 userId와 동일한 평문을 서비스에서 암호화하여 저장.
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createMember(
            @Valid @RequestBody MemberCreateRequest request) throws Exception {

        // 서비스 내부에서: userId 중복 체크 → 평문(userId) → BCrypt 등으로 암호화 → 저장
        memberService.createMemberWithIdEqualsPassword(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    // 수정: 일반 정보 전체 수정 가능(삭제 제외)
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            @PathVariable @Positive Long idx,
            @Valid @RequestBody MemberUpdateRequest request) throws Exception {

        memberService.updateMember(idx, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}