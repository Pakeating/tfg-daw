package com.inmopaco.AuctionService.application.usecases.impl;

import com.inmopaco.AuctionService.application.dto.AuctionDetailsDTO;
import com.inmopaco.AuctionService.application.usecases.AuctionsPersistenceUsecase;
import com.inmopaco.AuctionService.application.usecases.ProcessAuctionsUsecase;
import com.inmopaco.AuctionService.domain.enums.AuctionStatus;
import com.inmopaco.AuctionService.domain.enums.ProcessingStatus;
import com.inmopaco.AuctionService.infrastructure.messaging.queues.QueueService;
import com.inmopaco.AuctionService.infrastructure.pdf.PdfProcessingService;
import com.inmopaco.AuctionService.infrastructure.persistence.service.AuctionPersistenceService;
import com.inmopaco.shared.events.AIEvent;
import com.inmopaco.shared.events.AuctionsEvent;
import com.inmopaco.shared.events.GenericEventMsg;
import com.inmopaco.shared.events.enums.AIActions;
import com.inmopaco.shared.events.enums.Agents;
import com.inmopaco.shared.events.enums.AuctionsActions;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Log4j2
public class ProcessAuctionsUsecaseImpl implements ProcessAuctionsUsecase {

    private static final int EXPIRY_MARGIN_DAYS = 2;

    @Autowired
    private AuctionPersistenceService persistenceService;
    @Autowired
    private AuctionsPersistenceUsecase auctionsPersistenceUsecase;
    @Autowired
    private PdfProcessingService pdfProcessingService;
    @Autowired
    @Lazy
    private QueueService queueService;

    @Override
    public void processAuctions(AuctionsEvent event) {
        log.info("[ProcessAuctionsUsecase] START Processing auctions");
        var auctionsList = persistenceService.listAuctionsByProcessingStatus(ProcessingStatus.OBTAINED);

        log.info("[ProcessAuctionsUsecase] Found [" + auctionsList.size() + "] auctions to process");

        if (!auctionsList.isEmpty()) {
            Instant now = Instant.now();

            auctionsList.forEach(dto -> classifyAuctionStatus(dto, now));
            auctionsPersistenceUsecase.smartSaveObtainedAuctions(auctionsList);
        }

        log.info("[ProcessAuctionsUsecase] Re-evaluating already processed auctions for status updates");
        reevaluateAuctionStatuses();

        var responseEvent = AuctionsEvent.createEventMsg(AuctionsActions.PARTIALLY_PROCESSED_AUCTIONS,
                "Processed [" + auctionsList.size() + "] auctions");

        responseEvent.setParentEventId(event.getEventId());
        publish(responseEvent, Agents.ORCHESTRATOR_SERVICE);


        log.info("[ProcessAuctionsUsecase] Processing documents");
        auctionsList = persistenceService.listAuctionsByProcessingStatus(ProcessingStatus.PARTIALLY_PROCESSED);

        List<String> sent = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<String> alreadyProcessed = new ArrayList<>();
        List<String> blankDocs = new ArrayList<>();

        var auctionsWithDocs = auctionsList.stream()
                .filter(dto -> dto.getDocuments() != null && !dto.getDocuments().isEmpty())
                .toList();

        var auctionsNoDocs = auctionsList.stream()
                .filter(dto -> dto.getDocuments() == null || dto.getDocuments().isEmpty())
                .toList();

        if (!auctionsNoDocs.isEmpty()) {
            log.info("[ProcessAuctionsUsecase] Found [{}] auctions without documents, marking as PROCESSED", auctionsNoDocs.size());
            auctionsNoDocs.forEach(dto -> dto.setProcessingStatus(ProcessingStatus.PROCESSED));
            auctionsPersistenceUsecase.smartSaveObtainedAuctions(auctionsNoDocs);
        }

        auctionsWithDocs.forEach(dto -> {
                    boolean modified = false;
                    for (var doc : dto.getDocuments()) {
                        if (doc.getDocAiAnalysis() == null) { //si ya se ha generado no se repite
                            String docUrl = doc.getDocumentUrl();
                            String key = dto.getAuctionId().concat(" -> ").concat(docUrl);
                            try {
                                String value = doc.getExtractedText();
                                if (value == null || value.isBlank()) {
                                    log.info("[ProcessAuctionsUsecase] Extracting text from PDF for {}", key);
                                    value = pdfProcessingService.getTextFromPdfUrl(docUrl);
                                    doc.setExtractedText(value);
                                    modified = true;
                                } else {
                                    log.info("[ProcessAuctionsUsecase] Using already cached extracted text for {}", key);
                                }

                                if (value != null && !value.isBlank()) {
                                    var requestAIEvent = AIEvent.createEventMsg(AIActions.GET_AUCTIONS_REPORT, key);
                                    requestAIEvent.setContent(value);
                                    log.info("[ProcessAuctionsUsecase] Sending AI-Processing request for {}", key);
                                    publish(requestAIEvent, Agents.AI_SERVICE);
                                    sent.add(key);
                                } else {
                                    log.warn("[ProcessAuctionsUsecase] Extracted empty text from PDF for auction {}: {}", dto.getAuctionId(), docUrl);
                                    blankDocs.add(key);
                                }
                            } catch (Exception e) {
                                log.error("[ProcessAuctionsUsecase] Error processing or sending PDF for auction {}: {}", dto.getAuctionId(), e.getMessage());
                                failed.add(key);
                            }
                        } else {
                            alreadyProcessed.add(dto.getAuctionId());
                        }
                    }
                    if (modified) {
                        auctionsPersistenceUsecase.smartSaveObtainedAuctions(List.of(dto));
                    }
                });

        //eliminar posibles duplicados
        var alreadyProcessedFiltered = alreadyProcessed.stream().distinct().toList();
        log.info("[ProcessAuctionsUsecase] Finished sending AI-Processing requests. Sent: {}, Failed: {}, BlankDocs: {}, Already processed: {}",
                sent.size(), failed.size(), blankDocs.size(), alreadyProcessedFiltered.size()
        );

        /// procesar los docs en blanco, probablemente el pdf sea una foto o escaneado
        if (!blankDocs.isEmpty()) {
            log.warn("[ProcessAuctionsUsecase] The following documents resulted in blank text extraction:");
            var blankIds = blankDocs.stream()
                    .map(key -> key.split(" -> ")[0].trim())
                    .distinct()
                    .toList();
            var blankUrls = blankDocs.stream()
                    .map(key -> key.split(" -> ")[1].trim())
                    .toList();
            var auctions = persistenceService.findAllByAuctionIdIn(blankIds);
            var docs = auctions.stream()
                    .flatMap(dto -> dto.getDocuments().stream())
                    .filter(doc -> blankUrls.contains(doc.getDocumentUrl().trim()))
                    .peek(doc -> doc.setDocAiAnalysis("AI processing skipped due to blank text extraction"))
                    .toList();

            auctionsPersistenceUsecase.smartSaveAuctionDocs(docs);
        }

        /// ya procesados de antes, se marcan PROCESSED
        if (!alreadyProcessed.isEmpty()) {
            var auctions = persistenceService.findAllByAuctionIdIn(alreadyProcessedFiltered);
            auctions = auctions.stream()
                    .peek(dto -> dto.setProcessingStatus(ProcessingStatus.PROCESSED))
                    .toList();
            auctionsPersistenceUsecase.smartSaveObtainedAuctions(auctions);
        }

        /// errores
        if (!failed.isEmpty()) {
            log.error("[ProcessAuctionsUsecase] Failed to process or send the following documents:");
            failed.forEach(doc -> log.warn("\\\t{}", doc));
        }

        log.info("[ProcessAuctionsUsecase] END Processing auctions");
    }

    @Override
    public void processAuction(String auctionId) {
        log.info("[ProcessAuctionsUsecase] START Processing specific auction: [{}]", auctionId);
        var auctionOpt = persistenceService.findByAuctionId(auctionId);

        if (auctionOpt.isEmpty()) {
            log.warn("[ProcessAuctionsUsecase] Auction not found with ID: [{}]", auctionId);
            return;
        }

        var dto = auctionOpt.get();
        Instant now = Instant.now();

        if (dto.getDateOfEnd().isBefore(now.minus(EXPIRY_MARGIN_DAYS, ChronoUnit.DAYS))) {
            dto.setStatus(AuctionStatus.EXPIRED);
        } else if (dto.getDateOfStart().isAfter(now)) {
            dto.setStatus(AuctionStatus.UPCOMING);
        } else {
            dto.setStatus(AuctionStatus.ACTIVE);
        }
        dto.setProcessingStatus(ProcessingStatus.PARTIALLY_PROCESSED);

        auctionsPersistenceUsecase.smartSaveObtainedAuctions(List.of(dto));

        var responseEvent = AuctionsEvent.createEventMsg(AuctionsActions.PARTIALLY_PROCESSED_AUCTIONS,
                "Processed auction [" + auctionId + "]");

        publish(responseEvent, Agents.ORCHESTRATOR_SERVICE);

        log.info("[ProcessAuctionsUsecase] Processing documents for auction [{}]", auctionId);

        /// no diferencia si ya esta o no, se machaca lo que haya
        if (dto.getDocuments() != null) {
            boolean modified = false;
            for (var doc : dto.getDocuments()) {
                String docUrl = doc.getDocumentUrl();
                try {
                    String key = dto.getAuctionId().concat(" -> ").concat(docUrl);
                    String value = doc.getExtractedText();
                    
                    if (value == null || value.isBlank()) {
                        log.info("[ProcessAuctionsUsecase] Extracting text from PDF for {}", key);
                        value = pdfProcessingService.getTextFromPdfUrl(docUrl);
                        doc.setExtractedText(value);
                        modified = true;
                    } else {
                        log.info("[ProcessAuctionsUsecase] Using already cached extracted text for {}", key);
                    }

                    if (value != null && !value.isBlank()) {
                        var requestAIEvent = AIEvent.createEventMsg(AIActions.GET_AUCTIONS_REPORT, key);
                        requestAIEvent.setContent(value);
                        publish(requestAIEvent, Agents.AI_SERVICE);
                    }
                } catch (Exception e) {
                    log.error("[ProcessAuctionsUsecase] Error processing or sending PDF for auction {}: {}", dto.getAuctionId(), e.getMessage());
                }
            }
            if (modified) {
                auctionsPersistenceUsecase.smartSaveObtainedAuctions(List.of(dto));
            }
        }
        log.info("[ProcessAuctionsUsecase] END Processing auction [{}]", auctionId);
    }

    private void classifyAuctionStatus(AuctionDetailsDTO dto, Instant now) {
        if (dto.getDateOfEnd().isBefore(now.minus(EXPIRY_MARGIN_DAYS, ChronoUnit.DAYS))) {
            dto.setStatus(AuctionStatus.EXPIRED);
            dto.setProcessingStatus(ProcessingStatus.PROCESSED);
        } else if (dto.getDateOfStart().isAfter(now)) {
            dto.setStatus(AuctionStatus.UPCOMING);
            dto.setProcessingStatus(ProcessingStatus.PARTIALLY_PROCESSED);
        } else {
            dto.setStatus(AuctionStatus.ACTIVE);
            dto.setProcessingStatus(ProcessingStatus.PARTIALLY_PROCESSED);
        }
    }

    private void reevaluateAuctionStatuses() {
        Instant now = Instant.now();
        var partiallyProcessed = persistenceService.listAuctionsByProcessingStatus(ProcessingStatus.PARTIALLY_PROCESSED);
        var processed = persistenceService.listAuctionsByProcessingStatus(ProcessingStatus.PROCESSED);

        List<AuctionDetailsDTO> toUpdate = new ArrayList<>();

        Stream.concat(partiallyProcessed.stream(), processed.stream())
                .filter(dto -> dto.getStatus() != AuctionStatus.EXPIRED)
                .forEach(dto -> {
                    AuctionStatus previousStatus = dto.getStatus();
                    classifyAuctionStatus(dto, now);
                    if (previousStatus != dto.getStatus()) {
                        log.info("[ProcessAuctionsUsecase] Re-evaluating auction [{}]: {} -> {}",
                                dto.getAuctionId(), previousStatus, dto.getStatus());
                        toUpdate.add(dto);
                    }
                });

        if (!toUpdate.isEmpty()) {
            auctionsPersistenceUsecase.smartSaveObtainedAuctions(toUpdate);
            log.info("[ProcessAuctionsUsecase] Updated {} auction statuses after re-evaluation", toUpdate.size());
        }
    }

    @Transactional
    @Override
    public void saveAIProcessing(AIEvent event) {
        log.info("[ProcessAuctionsUsecase] START Saving AI processing for auction [{}]", event.getAuctionId());
        var keyParts = event.getAuctionId().split(" -> ");
        if (keyParts.length != 2) {
            log.error("[ProcessAuctionsUsecase] Invalid key format in AIEvent: {}", event.getAuctionId());
            return;
        }
        String auctionId = keyParts[0].trim();
        String docUrl = keyParts[1].trim();

        log.info("[ProcessAuctionsUsecase] Extracted auctionId: [{}], docUrl: [{}] from event key", auctionId, docUrl);
        log.info("[ProcessAuctionsUsecase] AI processing content: {}", event.getContent());

        try {
            var auctionOpt = persistenceService.findByAuctionId(auctionId);
            if (auctionOpt.isPresent()) {
                var auction = auctionOpt.get();
                auction.setProcessingStatus(ProcessingStatus.PROCESSED);
                auction.getDocuments().forEach(doc -> {
                    if (doc.getDocumentUrl().trim().equals(docUrl)) {
                        doc.setDocAiAnalysis(event.getContent());
                    }
                });
                auctionsPersistenceUsecase.smartSaveObtainedAuctions(List.of(auction));
            }

            var responseEvent = AuctionsEvent.createEventMsg(AuctionsActions.PROCESSED_AUCTIONS, auctionId);
            responseEvent.setParentEventId(event.getEventId());
            publish(responseEvent, Agents.ORCHESTRATOR_SERVICE);
            log.info("[ProcessAuctionsUsecase] Successfully saved AI processing for auction [{}]", auctionId);
        } catch (Exception e) {
            log.error("[ProcessAuctionsUsecase] Error saving AI processing for auction [{}]: {}", auctionId, e.getMessage());
        }
    }

    private void publish(GenericEventMsg event, Agents destinedTo) {
        event.setProducedBy(Agents.AUCTIONS_SERVICE);
        switch (destinedTo) {
            case ORCHESTRATOR_SERVICE -> {
                event.setDestinedTo(Agents.ORCHESTRATOR_SERVICE);
                queueService.publish("auctions.response", (AuctionsEvent) event);
            }

            case AI_SERVICE -> {
                event.setDestinedTo(Agents.AI_SERVICE);
                queueService.publish("ai.get", (AIEvent) event);
            }
            default -> event.setDestinedTo(Agents.ORCHESTRATOR_SERVICE);
        }

    }
}
