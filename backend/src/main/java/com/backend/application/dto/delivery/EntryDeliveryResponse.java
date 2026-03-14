package com.backend.application.dto.delivery;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EntryDeliveryResponse {

    private String uid;
    private Integer order;
    private String title;
    private String description;
    private Boolean isVisible;
    private String styleClasses;
    private ResponsiveMediaDeliveryResponse responsive;

    @JsonIgnore
    private Map<String, Object> customFields;

    @JsonAnyGetter
    public Map<String, Object> getCustomFieldsFlat() {
        return customFields;
    }
}
