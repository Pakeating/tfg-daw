package com.inmopaco.BFF.infrastructure.persistence.repository;

import com.inmopaco.BFF.infrastructure.persistence.entity.AuctionEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<AuctionEntity, String>, JpaSpecificationExecutor<AuctionEntity> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"documents", "lots"})
    Optional<AuctionEntity> findById(@NonNull String id);

    List<AuctionEntity> findAllByStatus(String currentStatus);

    List<AuctionEntity> findAllByAuctionIdIn(List<String> auctionId);

    List<AuctionEntity> findByCityIgnoreCase(String city);

    List<AuctionEntity> findByStatusAndDateOfEndAfter(String status, Instant now);

}
