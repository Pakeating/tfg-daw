package com.inmopaco.AuctionService.infrastructure.persistence.repository;

import com.inmopaco.AuctionService.infrastructure.persistence.entity.AuctionDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionDocumentRepository extends JpaRepository<AuctionDocumentEntity, String> {
}
