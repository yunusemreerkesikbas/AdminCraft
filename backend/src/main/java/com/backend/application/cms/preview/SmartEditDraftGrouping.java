package com.backend.application.cms.preview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SmartEditDraftGrouping {

    private SmartEditDraftGrouping() {
    }

    static List<SmartEditDraftGroupResponse> group(List<SmartEditDraftItemResponse> drafts) {
        Map<String, MutableGroup> groups = new LinkedHashMap<>();
        for (SmartEditDraftItemResponse draft : drafts) {
            if (draft.fieldChanges() == null || draft.fieldChanges().isEmpty()) {
                continue;
            }
            String key = groupKey(draft);
            MutableGroup group = groups.computeIfAbsent(key, ignored -> new MutableGroup(
                key,
                title(draft),
                subtitle(draft)));
            group.draftIds.add(draft.draftId());
            group.fields.addAll(draft.fieldChanges());
            group.updatedAt = latest(group.updatedAt, draft.updatedAt());
        }
        return groups.values().stream()
            .map(MutableGroup::toResponse)
            .sorted(Comparator.comparing(
                SmartEditDraftGroupResponse::updatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    static String groupKey(SmartEditDraftItemResponse draft) {
        if (draft.componentId() != null) {
            return "component:" + draft.componentId();
        }
        return "target:" + draft.targetType() + ":" + draft.targetId();
    }

    private static String title(SmartEditDraftItemResponse draft) {
        if (draft.componentName() != null && !draft.componentName().isBlank()) {
            return draft.componentName();
        }
        if (draft.componentUid() != null && !draft.componentUid().isBlank()) {
            return draft.componentUid();
        }
        return String.valueOf(draft.targetId());
    }

    private static String subtitle(SmartEditDraftItemResponse draft) {
        if (draft.componentUid() != null && !draft.componentUid().isBlank()) {
            return draft.componentUid();
        }
        if (draft.componentId() != null) {
            return "#" + draft.componentId();
        }
        return "";
    }

    private static LocalDateTime latest(LocalDateTime first, LocalDateTime second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return second.isAfter(first) ? second : first;
    }

    private static final class MutableGroup {
        private final String key;
        private final String title;
        private final String subtitle;
        private final List<Long> draftIds = new ArrayList<>();
        private final List<SmartEditDraftFieldChange> fields = new ArrayList<>();
        private LocalDateTime updatedAt;

        private MutableGroup(String key, String title, String subtitle) {
            this.key = key;
            this.title = title;
            this.subtitle = subtitle;
        }

        private SmartEditDraftGroupResponse toResponse() {
            return new SmartEditDraftGroupResponse(key, List.copyOf(draftIds), title, subtitle, List.copyOf(fields), updatedAt);
        }
    }
}
