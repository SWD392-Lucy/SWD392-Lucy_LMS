package com.lucy.contentlms.material.api.dto;

import jakarta.validation.constraints.NotNull;

public record PinMaterialRequest(
        @NotNull Long materialId
) {
}
