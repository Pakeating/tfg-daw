package com.inmopaco.PropertyService.infrastructure.persistence.repository;

import com.inmopaco.PropertyService.domain.enums.ProcessingStatus;
import com.inmopaco.PropertyService.infrastructure.persistence.entity.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, String> {

    List<PropertyEntity> findByCityIgnoreCase(String city);

    List<PropertyEntity> findByProvinceIgnoreCase(String province);

    List<PropertyEntity> findByContractType(com.inmopaco.PropertyService.domain.enums.ContractType contractType);

    List<PropertyEntity> findByProcessingStatus(ProcessingStatus processingStatus);

    boolean existsByUrl(String url);
}