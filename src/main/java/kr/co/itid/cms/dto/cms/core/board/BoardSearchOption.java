package kr.co.itid.cms.dto.cms.core.board;

import lombok.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoardSearchOption {
    private List<String> searchKeys;

    @Size(max = 100, message = "검색어는 100자 이하여야 합니다.")
    private String keyword;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "시작일은 yyyy-MM-dd 형식이어야 합니다.")
    private String startDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "종료일은 yyyy-MM-dd 형식이어야 합니다.")
    private String endDate;
}