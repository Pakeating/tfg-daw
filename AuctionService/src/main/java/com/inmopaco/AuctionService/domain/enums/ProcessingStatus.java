package com.inmopaco.AuctionService.domain.enums;

public enum ProcessingStatus {
    OBTAINED,
    PARTIALLY_PROCESSED,
    PROCESSED,
    ERROR,
    PENDING_REPROCESSING ///por si queremos recuperar subastas en error
}
