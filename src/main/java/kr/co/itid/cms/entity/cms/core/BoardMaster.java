package kr.co.itid.cms.entity.cms.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "is_use")
    private Boolean isUse = true;

    @Column(name = "board_type", length = 50)
    private String boardType;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @Column(name = "extends_option", columnDefinition = "TEXT")
    private String extendsOption;
}
