package com.inmopaco.AuctionService.infrastructure.persistence.mapper;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.infrastructure.persistence.entity.AuctionDocumentEntity;
import com.inmopaco.AuctionService.infrastructure.persistence.entity.AuctionEntity;
import com.inmopaco.AuctionService.infrastructure.persistence.entity.LotEntity;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public abstract class AuctionRepositoryMapper {

    @Autowired
    private LotRepositoryMapper lotMapper;

    public AuctionEntity toEntity(AuctionDetailsDTO auction) {
        if (auction == null) return null;
        
        AuctionEntity entity = toEntityInternal(auction);
        processLotsForNewEntity(auction, entity);
        return entity;
    }

    protected abstract AuctionEntity toEntityInternal(AuctionDetailsDTO auction);

    private void processLotsForNewEntity(AuctionDetailsDTO dto, AuctionEntity entity) {
        if (dto.getLots() == null || dto.getLots().isEmpty()) return;

        if (entity.getLots() == null) {
            entity.setLots(new HashSet<>());
        }

        dto.getLots().forEach(lotDTO -> {
            LotEntity lot = lotMapper.toEntity(lotDTO);
            lot.setAuction(entity);
            entity.getLots().add(lot);
        });
    }

    public abstract AuctionDetailsDTO toDTO(AuctionEntity auctionEntity);

    @Mapping(target = "documents", ignore = true)
    public abstract void updateEntityFromDTO(AuctionDetailsDTO dto, @MappingTarget AuctionEntity entity);

    /// Necesito los @AfterMapping porque se pierde la referencia de Hibernate y me da errores de detached entities y otros relacionados

    @AfterMapping
    void mergeDocuments(AuctionDetailsDTO dto, @MappingTarget AuctionEntity entity) {
        if (dto.getDocuments() == null) return;
        
        if (entity.getDocuments() == null) {
            entity.setDocuments(new ArrayList<>());
        }

        Map<String, AuctionDocumentEntity> currentDocs =
            entity.getDocuments().stream()
                  .collect(Collectors.toMap(
                      AuctionDocumentEntity::getDocumentUrl, 
                      doc -> doc,
                      (doc1, doc2) -> doc1));

        List<AuctionDocumentEntity> updatedDocs = new ArrayList<>();

        dto.getDocuments().forEach(newDoc -> {
            AuctionDocumentEntity doc = currentDocs.get(newDoc.getDocumentUrl());
            if (doc != null) {
                // original desde BD, solo actualizamos si el nuevo no es null
                if (newDoc.getDocAiAnalysis() != null) {
                    doc.setDocAiAnalysis(newDoc.getDocAiAnalysis());
                }
                if (newDoc.getExtractedText() != null) {
                    doc.setExtractedText(newDoc.getExtractedText());
                }
            } else {
                // nuevo docu
                doc = AuctionDocumentEntity.builder()
                        .documentUrl(newDoc.getDocumentUrl())
                        .docAiAnalysis(newDoc.getDocAiAnalysis())
                        .extractedText(newDoc.getExtractedText())
                        .build();
            }
            doc.setAuction(entity);
            updatedDocs.add(doc);
        });

        updatedDocs.forEach(doc -> {
            if (!entity.getDocuments().contains(doc)) {
                entity.getDocuments().add(doc);
            }
        });
    }

    @AfterMapping
    void mergeLots(AuctionDetailsDTO dto, @MappingTarget AuctionEntity entity) {
        if (dto.getLots() == null) return;
        
        if (entity.getLots() == null) {
            entity.setLots(new HashSet<>());
        }

        // Crea un mapa de los lotes actuales para actualizar sus valores en lugar de recrearlos
        Map<String, LotEntity> currentLots =
            entity.getLots().stream()
                  .collect(Collectors.toMap(
                      LotEntity::getLotId, 
                      l -> l, 
                      (l1, l2) -> l1));

        Set<LotEntity> updatedLots = new HashSet<>();

        dto.getLots().forEach(newLot -> {
            LotEntity lot = currentLots.get(newLot.getLotId());
            if (lot != null) {
                // lote existente: actualizar solo campos no nulos para preservar datos previos
                if (newLot.getLotTitle() != null) lot.setLotTitle(newLot.getLotTitle());
                if (newLot.getAuctionValue() != null) lot.setAuctionValue(newLot.getAuctionValue());
                if (newLot.getBidSteps() != null) lot.setBidSteps(newLot.getBidSteps());
                if (newLot.getDepositAmount() != null) lot.setDepositAmount(newLot.getDepositAmount());
                if (newLot.getGoodsDescription() != null) lot.setGoodsDescription(newLot.getGoodsDescription());
                if (newLot.getCadastralReference() != null) lot.setCadastralReference(newLot.getCadastralReference());
                if (newLot.getPropertyAddress() != null) lot.setPropertyAddress(newLot.getPropertyAddress());
                if (newLot.getCity() != null) lot.setCity(newLot.getCity());
                if (newLot.getProvince() != null) lot.setProvince(newLot.getProvince());
                if (newLot.getPossessionStatus() != null) lot.setPossessionStatus(newLot.getPossessionStatus());
                if (newLot.getPostalCode() != null) lot.setPostalCode(newLot.getPostalCode());
                if (newLot.getIsHabitualResidence() != null) lot.setIsHabitualResidence(newLot.getIsHabitualResidence());
                if (newLot.getIsVisitable() != null) lot.setIsVisitable(newLot.getIsVisitable());
            } else {
                // lote nuevo
                lot = lotMapper.toEntity(newLot);
            }
            // mantener la referencia a la auction
            lot.setAuction(entity);
            updatedLots.add(lot);
        });

        entity.getLots().addAll(updatedLots);
    }
}
