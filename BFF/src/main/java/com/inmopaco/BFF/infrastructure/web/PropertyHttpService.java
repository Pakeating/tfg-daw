package com.inmopaco.BFF.infrastructure.web;

import com.inmopaco.BFF.application.dto.PropertyDTO;
import com.inmopaco.BFF.application.dto.PropertyQueryDTO;
import com.inmopaco.BFF.application.usecases.PropertyQueryUsecase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/bff/properties")
public class PropertyHttpService {

    @Autowired
    private PropertyQueryUsecase propertyQueryUsecase;

    @Operation(summary = "Busca propiedades con filtros dinámicos",
            description = "Permite paginación y ordenación. El campo sortBy acepta '+' para ASC y '-' para DESC.")
    @PostMapping("/search")
    public ResponseEntity<Page<PropertyDTO>> searchProperties(
            @RequestBody PropertyQueryDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "+createdAt") String sortBy) {

        Pageable pageable = getPaginationAndSorting(page, size, sortBy);
        Page<PropertyDTO> result = propertyQueryUsecase.search(filter, pageable);
        log.info("Search executed with filters: {}, page: {}, size: {}, sortBy: {}", filter, page, size, sortBy);
        log.info("Search result: totalElements: {}, totalPages: {}", result.getTotalElements(), result.getTotalPages());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Busca propiedad por ID",
            description = "Busca una propiedad por su identificador único")
    @GetMapping("/search/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable String id) {
        log.info("GET /bff/properties/search/{} - Fetching property by id", id);
        return propertyQueryUsecase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Pageable getPaginationAndSorting(int page, int size, String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return PageRequest.of(page, size, Sort.unsorted());
        }

        Sort.Direction direction = Sort.Direction.ASC;
        String sortField = sortBy;

        if (sortBy.startsWith("-")) {
            direction = Sort.Direction.DESC;
            sortField = sortBy.substring(1).trim();
        } else if (sortBy.startsWith("+")) {
            sortField = sortBy.substring(1).trim();
        }

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}