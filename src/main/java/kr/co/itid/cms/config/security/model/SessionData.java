package kr.co.itid.cms.config.security.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redis에 저장될 세션 데이터
 * 최소한의 필수 정보만 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionData implements Serializable {
    
    private String userId;          // 사용자 ID
    private int userLevel;          // 사용자 레벨
    private Long idx;               // 사용자 인덱스
    private String userName;        // 사용자 이름
    private String hostname;        // 사이트 호스트명
    private long lastActivity;      // 마지막 활동 시간

    /**
     * 관리자 여부 확인
     */
    public boolean isAdmin() {
        return userLevel == 1;
    }
    
    /**
     * 현재 시간으로 마지막 활동 시간 업데이트
     */
    public void updateLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
}
