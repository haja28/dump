package com.makanforyou.order.repository;

import com.makanforyou.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all items in a cart
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Find cart item by cart ID and item ID
     */
    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    /**
     * Check if item exists in cart
     */
    boolean existsByCartIdAndItemId(Long cartId, Long itemId);

    /**
     * Delete all items in a cart
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);

    /**
     * Count items in cart
     */
    long countByCartId(Long cartId);
}
