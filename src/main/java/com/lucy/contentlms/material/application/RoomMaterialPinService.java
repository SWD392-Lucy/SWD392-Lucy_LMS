package com.lucy.contentlms.material.application;

import com.lucy.contentlms.material.api.dto.PinnedMaterialResponse;
import com.lucy.contentlms.material.domain.LearningMaterial;
import com.lucy.contentlms.material.domain.RoomPinnedMaterial;
import com.lucy.contentlms.material.infrastructure.RoomPinnedMaterialRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoomMaterialPinService {

    private final MaterialService materialService;
    private final RoomPinnedMaterialRepository pinnedMaterialRepository;

    public RoomMaterialPinService(
            MaterialService materialService,
            RoomPinnedMaterialRepository pinnedMaterialRepository
    ) {
        this.materialService = materialService;
        this.pinnedMaterialRepository = pinnedMaterialRepository;
    }

    @Transactional(readOnly = true)
    public List<PinnedMaterialResponse> listPinnedMaterials(UUID roomId) {
        return pinnedMaterialRepository.findByRoomIdOrderByDisplayOrderAscPinnedAtAsc(roomId)
                .stream()
                .map(PinnedMaterialResponse::from)
                .toList();
    }

    @Transactional
    public PinnedMaterialResponse pinMaterial(UUID roomId, Long materialId, Authentication authentication) {
        if (pinnedMaterialRepository.existsByRoomIdAndMaterialId(roomId, materialId)) {
            throw new IllegalArgumentException("Material is already pinned in this room.");
        }

        LearningMaterial material = materialService.getMaterial(materialId);
        int displayOrder = pinnedMaterialRepository.countByRoomId(roomId) + 1;
        RoomPinnedMaterial pin = new RoomPinnedMaterial(
                roomId,
                material,
                materialService.currentUserId(authentication),
                displayOrder
        );
        return PinnedMaterialResponse.from(pinnedMaterialRepository.save(pin));
    }

    @Transactional
    public void unpinMaterial(UUID roomId, Long pinId) {
        RoomPinnedMaterial pin = pinnedMaterialRepository.findByIdAndRoomId(pinId, roomId)
                .orElseThrow(() -> new IllegalArgumentException("Pinned material does not exist: " + pinId));
        pinnedMaterialRepository.delete(pin);
    }
}
