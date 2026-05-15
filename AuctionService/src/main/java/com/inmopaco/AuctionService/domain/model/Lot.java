package com.inmopaco.AuctionService.domain.model;

import lombok.Data;

import java.util.Map;

@Data
public class Lot {
    private Long lotId;
    private Long lotBoeNumber;
    private String description;
    private Double auctionValue;
    private Double currentBid;
    private String cadastralReference;
    private Map<String, String> cadastralData;
}
