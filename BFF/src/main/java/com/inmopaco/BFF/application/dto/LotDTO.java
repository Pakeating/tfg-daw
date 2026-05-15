package com.inmopaco.BFF.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LotDTO {

    private String lotId;
    private String lotTitle;

    private String auctionValue;
    private String bidSteps;
    private String depositAmount;

    private String goodsDescription;
    private String cadastralReference;
    private String propertyAddress;
    private String city;
    private String province;
    private String possessionStatus;

    private String postalCode;
    private String isHabitualResidence;
    private String isVisitable;
    private String auctionId; // Relación con la subasta principal

}
