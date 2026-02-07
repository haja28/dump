package com.makanforyou.menu.service;

import com.makanforyou.common.exception.ApplicationException;
import com.makanforyou.menu.dto.MenuLabelDTO;
import com.makanforyou.menu.entity.MenuLabel;
import com.makanforyou.menu.repository.MenuLabelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for menu label operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuLabelService {

    private final MenuLabelRepository labelRepository;

    /**
     * Create a new label
     */
    public MenuLabelDTO createLabel(String name, String description) {
        log.info("Creating label: {}", name);

        if (labelRepository.existsByName(name)) {
            throw new ApplicationException("LABEL_ALREADY_EXISTS",
                    "Label already exists with name: " + name);
        }

        MenuLabel label = MenuLabel.builder()
                .name(name)
                .description(description)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        label = labelRepository.save(label);
        log.info("Label created with ID: {}", label.getId());
        return mapToDTO(label);
    }

    /**
     * Get label by ID
     */
    public MenuLabelDTO getLabelById(Long labelId) {
        MenuLabel label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ApplicationException("LABEL_NOT_FOUND",
                        "Label not found"));
        return mapToDTO(label);
    }

    /**
     * Get all active labels
     */
    public List<MenuLabelDTO> getAllLabels() {
        log.info("Fetching all active labels");
        return labelRepository.findAll()
                .stream()
                .filter(MenuLabel::getIsActive)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update label
     */
    public MenuLabelDTO updateLabel(Long labelId, String name, String description) {
        MenuLabel label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ApplicationException("LABEL_NOT_FOUND",
                        "Label not found"));

        label.setName(name);
        label.setDescription(description);
        label.setUpdatedAt(LocalDateTime.now());
        label = labelRepository.save(label);

        log.info("Label updated with ID: {}", labelId);
        return mapToDTO(label);
    }

    /**
     * Deactivate label
     */
    public MenuLabelDTO deactivateLabel(Long labelId) {
        MenuLabel label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ApplicationException("LABEL_NOT_FOUND",
                        "Label not found"));

        label.setIsActive(false);
        label.setUpdatedAt(LocalDateTime.now());
        label = labelRepository.save(label);

        log.info("Label deactivated with ID: {}", labelId);
        return mapToDTO(label);
    }

    /**
     * Get label by name
     */
    public MenuLabel getLabelByName(String name) {
        return labelRepository.findByName(name)
                .orElseThrow(() -> new ApplicationException("LABEL_NOT_FOUND",
                        "Label not found with name: " + name));
    }

    /**
     * Map MenuLabel to DTO
     */
    private MenuLabelDTO mapToDTO(MenuLabel label) {
        return MenuLabelDTO.builder()
                .id(label.getId())
                .name(label.getName())
                .description(label.getDescription())
                .isActive(label.getIsActive())
                .build();
    }
}
