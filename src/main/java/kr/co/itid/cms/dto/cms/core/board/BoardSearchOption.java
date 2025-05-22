package kr.co.itid.cms.dto.cms.core.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BoardSearchOption {

    @NotBlank(message = "게시판 ID는 필수입니다.")
    private String boardId;

    private List<String> searchKeys;

    private String keyword;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "시작일은 yyyy-MM-dd 형식이어야 합니다.")
    private String startDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "종료일은 yyyy-MM-dd 형식이어야 합니다.")
    private String endDate;

    private Pageable pageable;
}