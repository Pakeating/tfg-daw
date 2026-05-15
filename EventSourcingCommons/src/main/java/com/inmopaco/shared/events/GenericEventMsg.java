package com.inmopaco.shared.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.inmopaco.shared.events.enums.Agents;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Log4j2
@Getter
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
public abstract class GenericEventMsg implements Serializable {

    private UUID eventId;
    protected LocalDateTime createdAt;
    protected LocalDateTime publishedAt;
    protected LocalDateTime consumedAt;
    @Setter
    protected Agents producedBy;
    @Setter
    protected Agents destinedTo;
    protected EventStatus status;
    protected boolean isPersistent;
    @Setter
    protected UUID parentEventId;

    protected GenericEventMsg(UUID eventId) {
        this.eventId = eventId;
        this.createdAt = LocalDateTime.now();
        this.isPersistent = true;
        this.changeStatus(EventStatus.CREATED);
    }

    public void published(LocalDateTime publishedAt) {
        changeStatus(EventStatus.PUBLISHED);
        this.publishedAt = publishedAt;
    }

    public void consumed(LocalDateTime consumedAt) {
        changeStatus(EventStatus.CONSUMED);

        this.consumedAt = consumedAt;
    }

    public void error() {
        changeStatus(EventStatus.ERROR);
    }

    public void changePersistence(boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    private void changeStatus(EventStatus newStatus){
        this.status = newStatus;
        log.info("Event {} status changed to {}", eventId, newStatus);
    }
}
