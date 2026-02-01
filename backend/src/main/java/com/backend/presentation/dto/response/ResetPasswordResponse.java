package com.backend.presentation.dto.response;

public record ResetPasswordResponse(String newPassword) {

  public static ResetPasswordResponse from(String newPassword) {
    return new ResetPasswordResponse(newPassword);
  }
}
