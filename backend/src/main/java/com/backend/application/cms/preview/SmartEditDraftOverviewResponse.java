package com.backend.application.cms.preview;

import java.util.List;

public record SmartEditDraftOverviewResponse(
    int count,
    List<SmartEditDraftItemResponse> drafts) {

    public static SmartEditDraftOverviewResponse of(List<SmartEditDraftItemResponse> drafts) {
        List<SmartEditDraftItemResponse> safeDrafts = drafts == null ? List.of() : drafts;
        return new SmartEditDraftOverviewResponse(safeDrafts.size(), safeDrafts);
    }
}
