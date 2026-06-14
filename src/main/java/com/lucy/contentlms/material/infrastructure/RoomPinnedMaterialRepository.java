package com.lucy.contentlms.material.infrastructure;

import com.lucy.contentlms.material.domain.RoomPinnedMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomPinnedMaterialRepository extends JpaRepository<RoomPinnedMaterial, Long> {

    List<RoomPinnedMaterial> findByRoomIdOrderByDisplayOrderAscPinnedAtAsc(UUID roomId);

    Optional<RoomPinnedMaterial> findByIdAndRoomId(Long id, UUID roomId);

    boolean existsByRoomIdAndMaterialId(UUID roomId, Long materialId);

    int countByRoomId(UUID roomId);

    void deleteByMaterialId(Long materialId);
}
