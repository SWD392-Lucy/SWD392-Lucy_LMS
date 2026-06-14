package com.lucy.contentlms.material.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room_pinned_materials", indexes = {
        @Index(name = "idx_room_pinned_materials_room", columnList = "room_id,display_order")
})
public class RoomPinnedMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private LearningMaterial material;

    @Column(name = "pinned_by_user_id", nullable = false)
    private UUID pinnedByUserId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "pinned_at", nullable = false)
    private Instant pinnedAt;

    protected RoomPinnedMaterial() {
    }

    public RoomPinnedMaterial(UUID roomId, LearningMaterial material, UUID pinnedByUserId, int displayOrder) {
        this.roomId = roomId;
        this.material = material;
        this.pinnedByUserId = pinnedByUserId;
        this.displayOrder = displayOrder;
        this.pinnedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public LearningMaterial getMaterial() {
        return material;
    }

    public UUID getPinnedByUserId() {
        return pinnedByUserId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Instant getPinnedAt() {
        return pinnedAt;
    }
}
