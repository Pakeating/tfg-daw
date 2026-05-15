package com.inmopaco.PropertyService.infrastructure.persistence.entity;

import com.inmopaco.PropertyService.domain.enums.ContractType;
import com.inmopaco.PropertyService.domain.enums.PropertyType;
import com.inmopaco.PropertyService.domain.enums.ProcessingStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 20)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", length = 20)
    private PropertyType propertyType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", length = 20)
    private ProcessingStatus processingStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (processingStatus == null) {
            processingStatus = ProcessingStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}