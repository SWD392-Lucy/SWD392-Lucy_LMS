package com.lucy.contentlms.material.api;

import com.lucy.contentlms.material.api.dto.PinMaterialRequest;
import com.lucy.contentlms.material.api.dto.PinnedMaterialResponse;
import com.lucy.contentlms.material.application.RoomMaterialPinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms/{roomId}/pinned-materials")
public class RoomMaterialController {

    private final RoomMaterialPinService pinService;

    public RoomMaterialController(RoomMaterialPinService pinService) {
        this.pinService = pinService;
    }

    @GetMapping
    public List<PinnedMaterialResponse> pinnedMaterials(@PathVariable UUID roomId) {
        return pinService.listPinnedMaterials(roomId);
    }

    @PostMapping
    public ResponseEntity<PinnedMaterialResponse> pinMaterial(
            @PathVariable UUID roomId,
            @Valid @RequestBody PinMaterialRequest request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pinService.pinMaterial(roomId, request.materialId(), authentication));
    }

    @DeleteMapping("/{pinId}")
    public ResponseEntity<Void> unpinMaterial(@PathVariable UUID roomId, @PathVariable Long pinId) {
        pinService.unpinMaterial(roomId, pinId);
        return ResponseEntity.noContent().build();
    }
}
