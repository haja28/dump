package com.makanforyou.menu.repository;

import com.makanforyou.menu.entity.MenuLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for MenuLabel entity
 */
@Repository
public interface MenuLabelRepository extends JpaRepository<MenuLabel, Long> {
    Optional<MenuLabel> findByName(String name);
    boolean existsByName(String name);
}
