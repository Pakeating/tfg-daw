package com.inmopaco.AuctionService.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auction_documents")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDocumentEntity {

    @Id
    @Column(name = "document_url")
    @EqualsAndHashCode.Include
    private String documentUrl;

    @Column(name = "doc_ai_analysis", columnDefinition = "LONGTEXT")
    private String docAiAnalysis;

    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private AuctionEntity auction;
}
