package com.inmopaco.BFF.infrastructure.web;

import com.inmopaco.BFF.application.dto.AuctionDetailsDTO;
import com.inmopaco.BFF.application.dto.AuctionQueryDTO;
import com.inmopaco.BFF.application.dto.ProvinceAuctionCount;
import com.inmopaco.BFF.application.usecases.AuctionQueryUsecase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/bff/auctions")
public class AuctionHttpService {

    @Autowired
    private AuctionQueryUsecase auctionUsecase;

    @Operation(summary = "Busca subastas por Id",
            description = "Busca subastas por su identificador único. Devuelve un solo resultado")
    @GetMapping("/search/{id}")
    public ResponseEntity<AuctionDetailsDTO> searchAuctionById( @PathVariable String id ) {

        var result = auctionUsecase.searchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

        log.info("Search executed for id {}", id);
//        log.info("Search result: {}", result);

        return result;

    }

    @Operation(summary = "Busca subastas con filtros dinámicos",
            description = "Permite paginación y ordenación. El campo sortBy acepta '+' para ASC y '-' para DESC.")
    @PostMapping("/search")
    public ResponseEntity<Page<AuctionDetailsDTO>> searchAuctions(
            @RequestBody AuctionQueryDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "+dateOfEnd") String sortBy) {

        Pageable pageable = getPaginationAndSorting(page, size, sortBy);
        Page<AuctionDetailsDTO> result = auctionUsecase.search(filter, pageable);
        log.info("Search executed with filters: {}, page: {}, size: {}, sortBy: {}", filter, page, size, sortBy);
        log.info("Search result: totalElements: {}, totalPages: {}", result.getTotalElements(), result.getTotalPages());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtiene número de subastas activas por provincia",
            description = "Devuelve el conteo de subastas con status ACTIVE agrupado por provincia")
    @GetMapping("/active-by-province")
    public ResponseEntity<List<ProvinceAuctionCount>> getActiveAuctionsCountByProvince() {
        log.info("Request received: get active auctions count by province");
        List<ProvinceAuctionCount> result = auctionUsecase.getActiveAuctionsCountByProvince();
        log.info("Returning {} active auctions by province records", result.size());
        return ResponseEntity.ok(result);
    }

    private Pageable getPaginationAndSorting (int page, int size, String sortBy) {

        Sort.Direction direction = Sort.Direction.ASC;
        String sortField = sortBy;

        if (sortBy == null || sortBy.isBlank()) {
            // default
            return PageRequest.of(page, size, Sort.unsorted());
        }

        if (sortBy.startsWith("-")) {
            direction = Sort.Direction.DESC;
            sortField = sortBy.substring(1).trim();
        } else {
            sortField = sortBy.substring(1).trim();
        }

        Sort sort = Sort.by(direction, sortField);
        return  PageRequest.of(page, size, sort);
    }
}
