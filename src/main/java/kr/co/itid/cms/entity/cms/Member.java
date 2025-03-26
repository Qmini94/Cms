package kr.co.itid.cms.entity.cms;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "egov_member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

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

    @Column(name = "dept_tel")
    private byte[] deptTel;

    @Column(name = "dept_fax")
    private byte[] deptFax;

    @Column(name = "dept_class")
    private String deptClass;

    @Column(name = "email")
    private byte[] email;

    @Column(name = "tel")
    private byte[] tel;

    @Column(name = "phone")
    private byte[] phone;

    @Column(name = "zipcode")
    private byte[] zipcode;

    @Column(name = "address1")
    private byte[] address1;

    @Column(name = "address2")
    private byte[] address2;

    @Column(name = "pass_hint_question")
    private String passHintQuestion;

    @Column(name = "pass_hint_answer")
    private byte[] passHintAnswer;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "recv_sms", nullable = false)
    private Boolean recvSms;

    @Column(name = "recv_mail", nullable = false)
    private Boolean recvMail;

    @Column(name = "foreigner")
    private Boolean foreigner;

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
