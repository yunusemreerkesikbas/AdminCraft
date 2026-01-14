package com.backend.application.dto.request;

import com.backend.presentation.validation.SlotName;
import com.backend.presentation.validation.Uid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.SLOT_NAME_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.UID_MAX_LENGTH;
import lombok.Data;

@Data
public class CreatePageSlotRequest {

  @Uid(required = false, maxLength = UID_MAX_LENGTH)
  private String uid;

  @SlotName(maxLength = SLOT_NAME_MAX_LENGTH)
  private String slotName;

  @NotBlank(message = "validation.slot.position.required")
  @Size(max = 20, message = "validation.slot.position.size")
  private String position;

  private Integer sortOrder = 0;

  private Boolean isShared = false;
}
