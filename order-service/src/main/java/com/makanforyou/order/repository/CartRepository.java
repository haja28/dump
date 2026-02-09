package com.makanforyou.order.repository;

import com.makanforyou.order.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if user has a cart
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete cart by user ID
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Find all carts that have not been updated since the given time (expired carts)
     */
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :expiredBefore")
    List<Cart> findExpiredCarts(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * Delete all expired carts (not updated since the given time)
     */
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.updatedAt < :expiredBefore")
    int deleteExpiredCarts(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * Count expired carts
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.updatedAt < :expiredBefore")
    long countExpiredCarts(@Param("expiredBefore") LocalDateTime expiredBefore);
}
