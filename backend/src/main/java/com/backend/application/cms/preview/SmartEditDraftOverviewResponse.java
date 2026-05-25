package com.backend.application.cms.preview;

import java.util.List;

public record SmartEditDraftOverviewResponse(
    int count,
    List<SmartEditDraftItemResponse> drafts,
    int groupCount,
    List<SmartEditDraftGroupResponse> groups) {

    public static SmartEditDraftOverviewResponse of(List<SmartEditDraftItemResponse> drafts) {
        List<SmartEditDraftItemResponse> safeDrafts = drafts == null ? List.of() : drafts;
        List<SmartEditDraftGroupResponse> groups = SmartEditDraftGrouping.group(safeDrafts);
        return new SmartEditDraftOverviewResponse(safeDrafts.size(), safeDrafts, groups.size(), groups);
    }
}
