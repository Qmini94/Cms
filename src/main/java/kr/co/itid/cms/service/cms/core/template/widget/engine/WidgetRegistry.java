package kr.co.itid.cms.service.cms.core.template.widget.engine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 위젯 핸들러 레지스트리
 * - 스프링 컨테이너에 등록된 모든 WidgetHandler를 수집/조회한다.
 * - type 문자열로 적절한 핸들러를 찾아 반환한다.
 */
@Component
@RequiredArgsConstructor
public class WidgetRegistry {

    private final List<WidgetHandler> handlers;

    /** 정확히 매칭되는 핸들러를 반환, 없으면 IllegalArgumentException */
    public WidgetHandler get(String type) {
        return find(type)
                .orElseThrow(() -> new IllegalArgumentException("Unknown widget type: " + type));
    }

    /** 존재하면 Optional로 반환 */
    public Optional<WidgetHandler> find(String type) {
        if (type == null || type.isBlank()) return Optional.empty();
        final String key = type.trim();
        return handlers.stream()
                .filter(h -> h.supports(key))
                .findFirst();
    }

    /** 등록된(지원하는) 위젯 타입 이름 목록 (디버그/문서화 용) */
    public List<String> supportedTypes() {
        return handlers.stream()
                .map(h -> h.getClass().getSimpleName())
                .collect(Collectors.toList());
    }
}