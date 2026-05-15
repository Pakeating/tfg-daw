package com.inmopaco.AuctionService.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuctionSummaryDTO {
    private String boeIdentifier;
    private String courtName;
    private String expediente;
    private String status;
    private String deadline;
    private String description;
    private String detailUrl;
}
