package com.inmopaco.AuctionService.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auction_lots")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotEntity {

    @Id
    @Column(name = "lot_id")
    @EqualsAndHashCode.Include
    private String lotId; // Usamos el lotId como PK si es único, si no, podrías usar un Long @GeneratedValue

    @Column(name = "lot_title", columnDefinition = "TEXT")
    private String lotTitle;

    @Column(name = "auction_value")
    private String auctionValue;

    @Column(name = "bid_steps")
    private String bidSteps;

    @Column(name = "deposit_amount")
    private String depositAmount;

    @Column(name = "goods_description", columnDefinition = "TEXT")
    private String goodsDescription;

    @Column(name = "cadastral_reference")
    private String cadastralReference;

    @Column(name = "property_address", columnDefinition = "TEXT")
    private String propertyAddress;

    private String city;
    private String province;

    @Column(name = "possession_status", columnDefinition = "TEXT")
    private String possessionStatus;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "is_habitual_residence")
    private String isHabitualResidence;

    @Column(name = "is_visitable", columnDefinition = "TEXT")
    private String isVisitable;

    // Relación con la subasta padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_auction_id")
    private AuctionEntity auction;
}
