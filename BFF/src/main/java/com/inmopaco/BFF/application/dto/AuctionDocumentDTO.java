package com.inmopaco.BFF.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDocumentDTO {
    private String documentUrl;
    private String docAiAnalysis;
}
