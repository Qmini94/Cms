package kr.co.itid.cms.entity.cms.core.member;

import kr.co.itid.cms.config.jpa.StringCryptoConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "egov_member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_nick")
    private String userNick;

    @Column(name = "user_level")
    private Integer userLevel;

    @Column(name = "user_pin", nullable = false)
    private String userPin;

    @Column(name = "user_password")
    private String userPassword;

    @Column(name = "group_idx")
    private Integer groupIdx;

    @Column(name = "dept_code")
    private String deptCode;

    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "dept_position")
    private String deptPosition;

    @Column(name = "dept_sort")
    private Integer deptSort;

    @Column(name = "dept_work")
    private String deptWork;

    // ===== 암/복호화 대상 (DB는 VARBINARY/BLOB) =====
    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "dept_tel")
    private String deptTel;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "dept_fax")
    private String deptFax;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "email")
    private String email;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "tel")
    private String tel;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "phone")
    private String phone;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "zipcode")
    private String zipcode;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "address1")
    private String address1;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "address2")
    private String address2;

    @Column(name = "pass_hint_question")
    private String passHintQuestion;

    @Convert(converter = StringCryptoConverter.class)
    @Column(name = "pass_hint_answer")
    private String passHintAnswer;
    // ===========================================

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "recv_sms")
    private String recvSms;

    @Column(name = "recv_mail")
    private String recvMail;

    @Column(name = "foreigner")
    private String foreigner;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "agree_date")
    private LocalDateTime agreeDate;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "info_update_date")
    private LocalDateTime infoUpdateDate;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "auth_token")
    private String authToken;

    @Column(name = "simple_pw")
    private String simplePw;

    @Column(name = "bookmark")
    private String bookmark;

    @Column(name = "is_sync")
    private Boolean isSync;
}
