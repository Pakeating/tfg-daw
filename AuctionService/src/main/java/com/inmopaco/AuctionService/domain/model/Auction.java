package com.inmopaco.AuctionService.domain.model;

import com.inmopaco.AuctionService.domain.enums.AuctionStatus;
import com.inmopaco.AuctionService.domain.enums.ProcessingStatus;
import lombok.Data;

import java.util.List;

@Data
public class Auction {
    private Long id;
    private String boeIdentifier;
    private String province;
    private String baseUrl;
    private String managingAuthority;
    private List<Lot> lots;
    private AuctionStatus status;
    private ProcessingStatus processingStatus;
}
