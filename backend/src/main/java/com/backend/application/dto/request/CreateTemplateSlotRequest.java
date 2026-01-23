package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.SLOT_NAME_MAX_LENGTH;

import java.util.List;

import com.backend.shared.validation.SlotName;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateSlotRequest {

  @SlotName(maxLength = SLOT_NAME_MAX_LENGTH)
  private String slotName;

  @NotBlank(message = "validation.slot.position.required")
  @Size(max = 20, message = "validation.slot.position.size")
  @Pattern(regexp = "^(TOP|CENTER|BOTTOM|LEFT|RIGHT)$", message = "validation.slot.position.pattern")
  private String position;

  @Min(value = 0, message = "validation.sortOrder.min")
  private Integer sortOrder = 0;

  private Boolean isRequired = false;

  @Min(value = 1, message = "validation.slot.maxComponents.min")
  private Integer maxComponents;

  @Size(max = 50, message = "validation.slot.allowedTypes.size")
  private List<@NotBlank(message = "validation.slot.allowedType.required") @Size(max = 100, message = "validation.slot.allowedType.size") String> allowedTypes;
}
