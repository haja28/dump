package com.makanforyou.menu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MenuItem Entity - Menu items offered by kitchens
 */
@Entity
@Table(name = "kitchen_menu", indexes = {
        @Index(name = "idx_kitchen_id", columnList = "kitchen_id"),
        @Index(name = "idx_item_name", columnList = "item_name"),
        @Index(name = "idx_is_active", columnList = "item_is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false, name = "kitchen_id")
    private Long kitchenId;

    @Column(nullable = false, length = 100, name = "item_name")
    private String itemName;

    @Column(length = 500, name = "item_description")
    private String description;

    @Column(length = 500, name = "item_ingredients")
    private String ingredients;

    @Column(length = 200, name = "item_allergic_indication")
    private String allergyIndication;

    @Column(nullable = false, precision = 10, scale = 2, name = "item_cost")
    private BigDecimal cost;

    @Column(length = 255, name = "item_image_path")
    private String imagePath;

    @Column(length = 100, name = "item_available_timing")
    private String availableTiming;

    @Column(name = "item_is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "preparation_time_minutes")
    @Builder.Default
    private Integer preparationTimeMinutes = 30;

    @Column(name = "item_quantity_available")
    private Integer quantityAvailable;

    @Column
    @Builder.Default
    private Boolean isVeg = true;

    @Column
    @Builder.Default
    private Boolean isHalal = false;

    @Column
    @Builder.Default
    private Integer spicyLevel = 1;

    @Column
    @Builder.Default
    private Double rating = 0.0;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "menu_item_labels",
        joinColumns = @JoinColumn(name = "menu_item_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @Builder.Default
    private Set<MenuLabel> labels = new HashSet<>();

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<MenuItemImage> images = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
