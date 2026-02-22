package com.makanforyou.kitchen.dto;

import com.makanforyou.kitchen.entity.Kitchen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for Kitchen response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenDTO {
    private Long id;
    private String kitchenName;
    private Long ownerUserId;
    private String ownerName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private String deliveryArea;
    private String cuisineTypes;
    private String ownerContact;
    private String ownerEmail;
    private String alternateContact;
    private String alternateEmail;
    private Kitchen.ApprovalStatus approvalStatus;
    private Double rating;
    private Integer totalOrders;
    private Boolean isActive;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
