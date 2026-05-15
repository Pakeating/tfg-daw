package com.inmopaco.BFF.infrastructure.persistence.impl;

import com.inmopaco.BFF.application.dto.AuctionDetailsDTO;
import com.inmopaco.BFF.application.dto.AuctionQueryDTO;
import com.inmopaco.BFF.application.dto.ProvinceAuctionCount;
import com.inmopaco.BFF.infrastructure.persistence.AuctionPersistenceService;
import com.inmopaco.BFF.infrastructure.persistence.entity.AuctionEntity;
import com.inmopaco.BFF.infrastructure.persistence.mapper.AuctionRepositoryMapper;
import com.inmopaco.BFF.infrastructure.persistence.repository.AuctionRepository;
import com.inmopaco.BFF.infrastructure.persistence.specification.AuctionSpecification;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Log4j2
@Service
public class AuctionPersistenceServiceImpl implements AuctionPersistenceService {

    @Autowired
    private AuctionRepository repository;

    @Autowired
    private AuctionRepositoryMapper mapper;

    @Override
    public List<AuctionDetailsDTO> listAllAuctions() {
        return repository.findAll().parallelStream().map(mapper::toDTO).toList();
    }

    @Override
    public Page<AuctionDetailsDTO> findAuctions(AuctionQueryDTO querySpecs, Pageable pageable) {

        Specification<AuctionEntity> spec = AuctionSpecification.getSpecification(querySpecs);
        return repository.findAll(spec, pageable).map(mapper::toDTO);
    }

    @Override
    public Optional<AuctionDetailsDTO> findAuctionById(String id) {
        return repository.findById(id).map(mapper::toDTO);
    }

    @Override
    public List<ProvinceAuctionCount> getActiveAuctionsCountByProvince() {
        log.info("Fetching active auctions count by province");
        Instant now = Instant.now();
        List<AuctionEntity> activeAuctions = repository.findByStatusAndDateOfEndAfter("ACTIVE", now);

        return activeAuctions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        auction -> auction.getProvince() != null ? auction.getProvince() : "UNKNOWN",
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .map(entry -> ProvinceAuctionCount.builder()
                        .province(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .toList();
    }

}
