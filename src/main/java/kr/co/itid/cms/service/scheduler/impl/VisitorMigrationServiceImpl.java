package kr.co.itid.cms.service.scheduler.impl;

import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.scheduler.VisitorMigrationService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service("visitorMigrationService")
@RequiredArgsConstructor
public class VisitorMigrationServiceImpl extends EgovAbstractServiceImpl implements VisitorMigrationService {

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final LoggingUtil loggingUtil;

    @Override
    @Scheduled(cron = "0 48 16 * * *") // 매일 00시 정각
    public void migrateDailyVisitorStats() throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to migrate daily visitor stats");

        try {
            Set<String> keys = redisTemplate.keys("visitors:daily:*");
            if (keys == null || keys.isEmpty()) {
                loggingUtil.logSuccess(Action.UPDATE, "No visitor data to migrate");
                return;
            }

            Map<String, Integer> webMap = new HashMap<>();
            Map<String, Integer> mobileMap = new HashMap<>();

            for (String key : keys) {
                try {
                    String[] parts = key.split(":");
                    if (parts.length != 5) continue;

                    String date = parts[2];
                    String hostname = parts[3];
                    String deviceType = parts[4];
                    String compoundKey = date + "|" + hostname;

                    int count = Integer.parseInt(redisTemplate.opsForValue().get(key));
                    if ("web".equals(deviceType)) {
                        webMap.merge(compoundKey, count, Integer::sum);
                    } else if ("mobile".equals(deviceType)) {
                        mobileMap.merge(compoundKey, count, Integer::sum);
                    }

                    redisTemplate.delete(key);
                } catch (Exception e) {
                    loggingUtil.logFail(Action.UPDATE, "Skip broken key: " + key + " - " + e.getMessage());
                    // 계속해서 다음 key 처리 필요. catch 만하고 예외 던지면 안됨.
                }
            }

            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(webMap.keySet());
            allKeys.addAll(mobileMap.keySet());

            for (String compoundKey : allKeys) {
                String[] parts = compoundKey.split("\\|");
                String date = parts[0];
                String hostname = parts[1];
                int webCount = webMap.getOrDefault(compoundKey, 0);
                int mobileCount = mobileMap.getOrDefault(compoundKey, 0);

                try {
                    jdbcTemplate.update("""
                        INSERT INTO visit_site (visit_date, hostname, web_count, mobile_count)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            web_count = web_count + VALUES(web_count),
                            mobile_count = mobile_count + VALUES(mobile_count)
                    """, LocalDate.parse(date), hostname, webCount, mobileCount);
                } catch (DataAccessException e) {
                    loggingUtil.logFail(Action.UPDATE, "DB error: " + e.getMessage());
                    throw processException("DB error. " + e.getMessage(), e);
                }
            }

            loggingUtil.logSuccess(Action.UPDATE, "Visitor stats migration done");

        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Migration failed: " + e.getMessage());
            throw processException("Migration failed. " + e.getMessage(), e);
        }
    }
}