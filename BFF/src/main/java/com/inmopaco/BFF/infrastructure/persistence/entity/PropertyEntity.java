package com.inmopaco.BFF.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyEntity {

    @Id
    @Column(name = "property_id", length = 50)
    private String propertyId;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "autonomous_community", length = 100)
    private String autonomousCommunity;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "zone", length = 100)
    private String zone;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "contract_type", length = 20)
    private String contractType;

    @Column(name = "property_type", length = 20)
    private String propertyType;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "surface")
    private Integer surface;

    @Column(name = "elevator")
    private Boolean elevator;

    @Column(name = "heating", length = 50)
    private String heating;

    @Column(name = "floor", length = 20)
    private String floor;

    @Column(name = "year_construction")
    private Integer yearConstruction;

    @Column(name = "energy_class", length = 5)
    private String energyClass;

    @Column(name = "energy_consumption", length = 20)
    private String energyConsumption;

    @Column(name = "price")
    private Long price;

    @Column(name = "publish_date")
    private Instant publishDate;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "processing_status", length = 20)
    private String processingStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}