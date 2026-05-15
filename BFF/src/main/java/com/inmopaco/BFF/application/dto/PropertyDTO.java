package com.inmopaco.BFF.application.dto;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDTO {
    private String propertyId;
    private String url;
    private String country;
    private String autonomousCommunity;
    private String province;
    private String city;
    private String zone;
    private String street;
    private String contractType;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer surface;
    private Boolean elevator;
    private String heating;
    private String floor;
    private Integer yearConstruction;
    private String energyClass;
    private String energyConsumption;
    private Long price;
    private Instant publishDate;
    private String title;
    private String description;
    private String imageUrls;
    private String processingStatus;
    private Instant createdAt;
    private Instant updatedAt;
}