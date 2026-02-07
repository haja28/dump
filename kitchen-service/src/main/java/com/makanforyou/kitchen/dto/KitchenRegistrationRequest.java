package com.makanforyou.kitchen.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for kitchen registration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenRegistrationRequest {

    @NotBlank(message = "Kitchen name is required")
    @Size(min = 3, max = 100, message = "Kitchen name must be between 3 and 100 characters")
    private String kitchenName;

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String state;

    @Size(max = 10)
    private String postalCode;

    @Size(max = 50)
    private String country;

    @DecimalMin("-90")
    @DecimalMax("90")
    private Double latitude;

    @DecimalMin("-180")
    @DecimalMax("180")
    private Double longitude;

    @Size(max = 255)
    private String deliveryArea;

    @Size(max = 500)
    private String cuisineTypes;

    @NotBlank(message = "Owner contact is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Owner contact must be 10-15 digits")
    private String ownerContact;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be valid")
    private String ownerEmail;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Alternate contact must be 10-15 digits")
    private String alternateContact;

    @Email(message = "Alternate email must be valid")
    private String alternateEmail;
}
