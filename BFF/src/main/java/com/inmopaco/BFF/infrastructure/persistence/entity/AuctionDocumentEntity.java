package com.inmopaco.BFF.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDocumentEntity {

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "doc_ai_analysis", columnDefinition = "TEXT")
    private String docAiAnalysis;
}
