package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.SLOT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.SORT_ORDER_MIN;
import static com.backend.shared.constants.ValidationConstants.UID_MAX_LENGTH;

import com.backend.shared.validation.SlotName;
import com.backend.shared.validation.Uid;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePageSlotRequest {

  @Uid(required = false, maxLength = UID_MAX_LENGTH)
  private String uid;

  @SlotName(maxLength = SLOT_NAME_MAX_LENGTH)
  private String slotName;

  @NotBlank(message = "validation.slot.position.required")
  @Size(max = 20, message = "validation.slot.position.size")
  private String position;

  @Min(value = SORT_ORDER_MIN, message = "validation.sort.order.min")
  private Integer sortOrder;

  private Boolean isActive;

  private Boolean isShared;
}
