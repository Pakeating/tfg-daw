package com.inmopaco.BFF.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuctionQueryDTO {

    private List<String> auctionIds; // Identificadores

    private String type;             // Tipo de subasta

    private String dateOfEnd;        // Fecha de conclusión

    private String city;                 // Localidad

    private List<String> provinces;      // Provincias

    private String isVisitable;         // Visitable

    private Boolean hasBids; // ¿Ha recibido pujas?

    private String status;
    
    private Boolean isMultiLot; // ¿Es multilote?
}
