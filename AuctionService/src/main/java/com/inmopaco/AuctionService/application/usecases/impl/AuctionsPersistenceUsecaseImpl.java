package com.inmopaco.AuctionService.application.usecases.impl;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.dto.AuctionDocumentDTO;
import com.inmopaco.AuctionService.application.usecases.AuctionsPersistenceUsecase;
import com.inmopaco.AuctionService.domain.enums.ProcessingStatus;
import com.inmopaco.AuctionService.infrastructure.persistence.entity.AuctionDocumentEntity;
import com.inmopaco.AuctionService.infrastructure.persistence.repository.AuctionDocumentRepository;
import com.inmopaco.AuctionService.infrastructure.persistence.service.AuctionPersistenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class AuctionsPersistenceUsecaseImpl implements AuctionsPersistenceUsecase {

    @Autowired
    private AuctionPersistenceService persistenceService;

    @Autowired
    private AuctionDocumentRepository documentRepository;

    @Override
    public void smartSaveObtainedAuctions(List<AuctionDetailsDTO> auctionList) {

        try {
            log.info("[smartSaveObtainedAuctions] Saving {} Auctions", auctionList.size());

            //Smart-saving: check existing by boeIdentifier and update, else create new
            List<String> identifiers = auctionList.stream()
                    .map(AuctionDetailsDTO::getAuctionId)
                    .toList();

            Map<String, AuctionDetailsDTO> existingMap = persistenceService.findAllByAuctionIdIn(identifiers)
                    .stream()
                    .collect(Collectors.toMap(AuctionDetailsDTO::getAuctionId, dto -> dto));

            auctionList.forEach(dto -> {
                if (!existingMap.containsKey(dto.getAuctionId())) {
                    dto.setProcessingStatus(ProcessingStatus.OBTAINED);
                }
            });

            log.info("Saving {} auctions", auctionList.size());
            log.debug("Auction IDs: {}", auctionList.stream().map(AuctionDetailsDTO::getAuctionId).toList());

            persistenceService.saveAllAuctions(auctionList);

            long created = auctionList.size() - existingMap.size();

            log.info("[smartSaveObtainedAuctions] Finished smartSaving {} Auctions, {} new created", auctionList.size(), created);
        } catch (Exception e) {
            log.warn("[smartSaveObtainedAuctions] Error saving auctions: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void smartSaveAuctionDocs(List<AuctionDocumentDTO> docs) {
        if (docs == null || docs.isEmpty()) return;

        try {
            log.info("[smartSaveAuctionDocs] Processing {} Documents", docs.size());
            
            List<String> urls = docs.stream()
                    .map(AuctionDocumentDTO::getDocumentUrl)
                    .toList();

            Map<String, AuctionDocumentEntity> existingMap =
                    documentRepository.findAllById(urls).stream()
                    .collect(Collectors.toMap(AuctionDocumentEntity::getDocumentUrl, doc -> doc));

            List<AuctionDocumentEntity> toSave = new java.util.ArrayList<>();

            docs.forEach(dto -> {
                var entity = existingMap.get(dto.getDocumentUrl());
                if (entity != null) {
                    // solo actualizamos los campos modificados...
                    boolean modified = false;
                    if (dto.getDocAiAnalysis() != null && !dto.getDocAiAnalysis().equals(entity.getDocAiAnalysis())) {
                        entity.setDocAiAnalysis(dto.getDocAiAnalysis());
                        modified = true;
                    }
                    if (dto.getExtractedText() != null && !dto.getExtractedText().equals(entity.getExtractedText())) {
                        entity.setExtractedText(dto.getExtractedText());
                        modified = true;
                    }
                    if (modified) {
                        toSave.add(entity);
                    }
                } else {
                    // Create new entity
                    toSave.add(AuctionDocumentEntity.builder()
                            .documentUrl(dto.getDocumentUrl())
                            .docAiAnalysis(dto.getDocAiAnalysis())
                            .extractedText(dto.getExtractedText())
                            .build());
                }
            });

            if (!toSave.isEmpty()) {
                documentRepository.saveAll(toSave);
                log.info("[smartSaveAuctionDocs] Successfully saved {} modified documents", toSave.size());
            } else {
                log.info("[smartSaveAuctionDocs] No changes detected in the documents");
            }

        } catch (Exception e) {
            log.warn("[smartSaveAuctionDocs] Error saving documents: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteAuction(String auctionId) {
        log.info("[deleteAuction] Deleting auction: {}", auctionId);
        persistenceService.deleteAuction(auctionId);
    }
}
