package kr.co.itid.cms.dto.cms.core.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaginationOption {

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page;  // int → Integer (null 허용)

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 50, message = "페이지 크기는 50 이하여야 합니다.")
    private Integer size;  // int → Integer (null 허용)

    @Pattern(regexp = "^[a-zA-Z0-9_,]+$", message = "정렬 필드는 영문, 숫자, 쉼표만 사용할 수 있습니다.")
    private String sort;  // null 가능

    public Pageable toPageable() {
        if (page == null || size == null || sort == null) {
            return null;
        }

        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            String direction = sortParams.length > 1 ? sortParams[1].toUpperCase() : "DESC";

            return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortField));
        } catch (Exception e) {
            return null;
        }
    }
}


