package com.inmopaco.AuctionService.infrastructure.persistence.service.impl;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.domain.enums.AuctionStatus;
import com.inmopaco.AuctionService.domain.enums.ProcessingStatus;
import com.inmopaco.AuctionService.infrastructure.persistence.entity.AuctionEntity;
import com.inmopaco.AuctionService.infrastructure.persistence.mapper.AuctionRepositoryMapper;
import com.inmopaco.AuctionService.infrastructure.persistence.repository.AuctionRepository;
import com.inmopaco.AuctionService.infrastructure.persistence.service.AuctionPersistenceService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class AuctionPersistenceServiceImpl implements AuctionPersistenceService {

    @Autowired
    private AuctionRepository repository;

    @Autowired
    private AuctionRepositoryMapper mapper;

    @Override
    public void saveAuction(AuctionDetailsDTO auctionDTO) {
        repository.save(mapper.toEntity(auctionDTO));
    }

    @Override
    public Optional<AuctionDetailsDTO> findByAuctionId(String auctionId) {
        return repository.findById(auctionId)
                .map(mapper::toDTO);
    }

    @Override
    public List<AuctionDetailsDTO> findAllByAuctionIdIn(List<String> identifiers) {
        return repository.findAllByAuctionIdIn(identifiers)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public void saveAllAuctions(List<AuctionDetailsDTO> auctionList) {
        if (auctionList == null || auctionList.isEmpty()) {
            return;
        }

        List<String> identifiers = auctionList.stream()
                .map(AuctionDetailsDTO::getAuctionId)
                .toList();

        Map<String, AuctionEntity> entityMap = repository.findAllByAuctionIdIn(identifiers)
                .stream()
                .collect(Collectors.toMap(AuctionEntity::getAuctionId, entity -> entity));

        List<AuctionEntity> entitiesToSave = auctionList.stream().map(dto -> {
            AuctionEntity existingEntity = entityMap.get(dto.getAuctionId());

            if (existingEntity != null) {
                mapper.updateEntityFromDTO(dto, existingEntity);
                return existingEntity;
            } else {
                try {
                    return mapper.toEntity(dto);
                } catch (Throwable e) {
                    log.error("Error mapping DTO to Entity for auctionId: {}. Error: {}, with stackTrace: {}", dto.getAuctionId(), e.getMessage(), e.getStackTrace());
                    return null;
                }
            }
        }).filter(java.util.Objects::nonNull).toList();

        repository.saveAll(entitiesToSave);
    }

    @Override
    public List<AuctionDetailsDTO> listAllAuctions() {
        return repository.findAll().parallelStream().map(mapper::toDTO).toList();
    }

    @Override
    public List<String> listAuctionIdsByStatus(AuctionStatus status) {
        return repository.findAllByStatus(status).stream().map(AuctionEntity::getAuctionId).toList();
    }

    @Override
    public List<AuctionDetailsDTO> listAuctionsByProcessingStatus(ProcessingStatus status) {
        List<AuctionEntity> auctionList;

        if (status.equals(ProcessingStatus.OBTAINED)) {
            auctionList = repository.findAllByProcessingStatus(status);
            auctionList.addAll(repository.findAllByProcessingStatus(null)); //por si queda alguna con null
        }else {
            auctionList = repository.findAllByProcessingStatus(status);
        }

        return auctionList.parallelStream().map(mapper::toDTO).toList();
    }
    
    @Override
    @Transactional
    public void deleteAuction(String auctionId) {
        log.info("[deleteAuction] Deleting auction with id: {}", auctionId);
        repository.deleteById(auctionId);
    }
}
