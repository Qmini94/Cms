package kr.co.itid.cms.dto.cms.core.board.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BoardRequest {

    @NotBlank(message = "게시판 ID는 필수입니다")
    private String boardId;

    private String departName;
    private String regPin;
    private String regId;
    private String regName;
    private String regIp;

    @Size(max = 100, message = "태그는 100자 이하로 입력해주세요")
    private String searchTag;

    private Boolean isTopFixed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime topEnd;

    private Integer pidx;

    private Integer level;

    private Integer seq;

    @DecimalMin(value = "0.0", inclusive = true, message = "정렬값은 0 이상이어야 합니다")
    private Double sort;

    private String adminComment;
    private String adminCommentTo;

    @Pattern(regexp = "^(public|private)$", message = "공개상태는 public 또는 private 이어야 합니다")
    private String openStatus;

    private Boolean isApproved;

    private String category1;
    private String category2;
    private String process1;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    private Long mainimageIdx;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "시작일은 yyyy-MM-dd 형식이어야 합니다")
    private String periodStart;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "종료일은 yyyy-MM-dd 형식이어야 합니다")
    private String periodEnd;

    private String contentsOriginal;
}
