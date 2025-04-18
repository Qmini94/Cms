package kr.co.itid.cms.entity.cms.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "board_master")
@Getter
@Setter
@NoArgsConstructor
public class BoardMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false, unique = true, length = 50)
    private String boardId;

    @Column(name = "board_name", nullable = false, length = 100)
    private String boardName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "use_yn")
    private Boolean useYn = true;

    @Column(name = "skin_type", length = 50)
    private String skinType;

    @Column(name = "reg_date", nullable = false)
    private Integer regDate = 0;

    @Column(name = "updated_date", nullable = false)
    private Integer updatedDate = 0;

    @Column(name = "extends_option", columnDefinition = "TEXT")
    private String extendsOption;
}
