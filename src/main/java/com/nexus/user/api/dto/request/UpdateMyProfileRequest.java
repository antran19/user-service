package com.nexus.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A blank or missing phone clears the phone number.
public record UpdateMyProfileRequest(
        @NotBlank @Size(max = 255) String fullName,
        @Size(max = 20) String phone) {
}
