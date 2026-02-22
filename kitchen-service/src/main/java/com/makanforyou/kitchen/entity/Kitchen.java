package com.makanforyou.kitchen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Kitchen Entity - Represents home-based kitchen
 */
@Entity
@Table(name = "kitchens", indexes = {
        @Index(name = "idx_kitchen_name", columnList = "kitchen_name"),
        @Index(name = "idx_city", columnList = "kitchen_city"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kitchen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kitchen_id")
    private Long id;

    @Column(nullable = false, length = 100, name = "kitchen_name")
    private String kitchenName;

    @Column(nullable = false, name = "owner_user_id")
    private Long ownerUserId;

    @Column(nullable = false, length = 100, name = "kitchen_owner_name")
    private String ownerName;

    @Column(length = 255, name = "kitchen_description")
    private String description;

    @Column(length = 255, nullable = false, name = "kitchen_address")
    private String address;

    @Column(length = 50, name = "kitchen_city")
    private String city;

    @Column(length = 50, name = "kitchen_state")
    private String state;

    @Column(length = 10, name = "kitchen_postal_code")
    private String postalCode;

    @Column(length = 50, name = "kitchen_country")
    private String country;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 255, name = "delivery_area")
    private String deliveryArea;

    @Column(length = 500, name = "cuisine_types")
    private String cuisineTypes;

    @Column(length = 15, name = "kitchen_owner_contact")
    private String ownerContact;

    @Column(length = 100, name = "kitchen_owner_email")
    private String ownerEmail;

    @Column(length = 15, name = "kitchen_alternate_contact")
    private String alternateContact;

    @Column(length = 100, name = "kitchen_alternate_email")
    private String alternateEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "approval_status")
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "total_orders")
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(nullable = false, name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "verified")
    @Builder.Default
    private Boolean verified = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Kitchen approval status
     */
    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
