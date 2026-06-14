package com.lucy.contentlms.material.api.dto;

import com.lucy.contentlms.material.domain.LearningMaterial;
import com.lucy.contentlms.material.domain.RoomPinnedMaterial;

import java.time.Instant;
import java.util.UUID;

public record PinnedMaterialResponse(
        Long id,
        UUID roomId,
        int displayOrder,
        UUID pinnedByUserId,
        Instant pinnedAt,
        MaterialResponse material
) {
    public static PinnedMaterialResponse from(RoomPinnedMaterial pin) {
        LearningMaterial material = pin.getMaterial();
        return new PinnedMaterialResponse(
                pin.getId(),
                pin.getRoomId(),
                pin.getDisplayOrder(),
                pin.getPinnedByUserId(),
                pin.getPinnedAt(),
                MaterialResponse.from(material)
        );
    }
}
