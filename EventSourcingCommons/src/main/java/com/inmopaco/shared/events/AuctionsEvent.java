package com.inmopaco.shared.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.inmopaco.shared.events.enums.AuctionsActions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
public class AuctionsEvent extends GenericEventMsg {
    private AuctionsActions action;
    @Setter
    private String payload;

    private AuctionsEvent(UUID eventId, AuctionsActions action, String payload) {
        super(eventId);
        this.action = action;
        this.payload = payload;
    }

    public static AuctionsEvent createEventMsg(AuctionsActions action, String payload) {
        return new AuctionsEvent(UUID.randomUUID(), action, payload);
    }

    public static AuctionsEvent createEventMsg(AuctionsActions action) {
        return new AuctionsEvent(UUID.randomUUID(), action, null);
    }

}
