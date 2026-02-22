package com.makanforyou.menu.specification;

import com.makanforyou.menu.entity.MenuItem;
import com.makanforyou.menu.dto.MenuSearchFilter;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications for dynamic filtering of menu items
 */
public class MenuItemSpecification {

    public static Specification<MenuItem> withDynamicFilters(MenuSearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active status
            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            // Search query
            if (filter.getQuery() != null && !filter.getQuery().isEmpty()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("itemName")),
                    "%" + filter.getQuery().toLowerCase() + "%");
                Predicate descPredicate = cb.like(cb.lower(root.get("description")),
                    "%" + filter.getQuery().toLowerCase() + "%");
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            // Kitchen filter
            if (filter.getKitchenId() != null) {
                predicates.add(cb.equal(root.get("kitchenId"), filter.getKitchenId()));
            }

            // Price range
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("cost"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("cost"), filter.getMaxPrice()));
            }

            // Vegetarian filter
            if (filter.getIsVeg() != null) {
                predicates.add(cb.equal(root.get("isVeg"), filter.getIsVeg()));
            }

            // Halal filter
            if (filter.getIsHalal() != null) {
                predicates.add(cb.equal(root.get("isHalal"), filter.getIsHalal()));
            }

            // Spicy level range
            if (filter.getMinSpicyLevel() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("spicyLevel"), filter.getMinSpicyLevel()));
            }
            if (filter.getMaxSpicyLevel() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("spicyLevel"), filter.getMaxSpicyLevel()));
            }

            // Labels filter
            if (filter.getLabels() != null && !filter.getLabels().isEmpty()) {
                predicates.add(root.get("labels").in(filter.getLabels()));
            }

            // Order by
            if (filter.getSortBy() != null) {
                switch (filter.getSortBy()) {
                    case "rating_desc":
                        query.orderBy(cb.desc(root.get("rating")));
                        break;
                    case "price_asc":
                        query.orderBy(cb.asc(root.get("cost")));
                        break;
                    case "price_desc":
                        query.orderBy(cb.desc(root.get("cost")));
                        break;
                    case "newest":
                        query.orderBy(cb.desc(root.get("createdAt")));
                        break;
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
