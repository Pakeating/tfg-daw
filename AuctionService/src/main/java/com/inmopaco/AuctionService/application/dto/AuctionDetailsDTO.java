package com.inmopaco.AuctionService.application.dto;

import com.inmopaco.AuctionService.domain.enums.AuctionStatus;
import com.inmopaco.AuctionService.domain.enums.ProcessingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AuctionDetailsDTO {
    private String auctionId;        // Identificador
    private String type;             // Tipo de subasta
    private String countingAccount;  // Cuenta expediente
    private Instant dateOfStart;      // Fecha de inicio
    private Instant dateOfEnd;        // Fecha de conclusión
    private String claimedAmount;    // Cantidad reclamada
    private String lotsNumber;       // Lotes
    private String boeAnnouncement;  // Anuncio BOE
    private String auctionValue;     // Valor subasta
    private String appraisal;        // Tasación
    private String minimumBid;       // Puja mínima
    private String bidIncrements;    // Tramos entre pujas
    private String depositAmount;    // Importe del depósito
    private List<AuctionDocumentDTO> documents; //documentos asociados
    private String authorityCode;        // Código
    private String authorityDescription; // Descripción (Nombre del Juzgado)
    private String authorityAddress;     // Dirección
    private String authorityPhone;       // Teléfono
    private String authorityFax;         // Fax
    private String authorityEmail;       // Correo electrónico
    private String goodsDescription;       // Descripción de los bienes
    private String cadastralReference;   // Referencia Catastral
    private String propertyAddress;      // Dirección de los bienes
    private String cru;                  // Código Registral Único
    private String registryData;         // Datos registrales (Tomos, Libros, etc.)
    private String city;                 // Localidad
    private String province;             // Provincia
    private String isHabitualResidence;   // Vivienda habitual
    private String isVisitable;         // Visitable
    private String postalCode;           // Código Postal
    private String possessionStatus;     // Situación posesoria
    private String creditorName;      // Nombre del Acreedor
    private String creditorNif;       // NIF del Acreedor
    private String creditorAddress;   // Dirección del Acreedor
    private boolean hasBids; // ¿Ha recibido pujas?
    private List<LotDTO> lots; // Lotes de la subasta

    private String courtName;//ESTOS TRES PROVIENEN DE SUMMARY
    private String expediente;
    private AuctionStatus status;
    private ProcessingStatus processingStatus;
}
