package com.inmopaco.shared.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.inmopaco.shared.events.enums.AIActions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@NoArgsConstructor(onConstructor_ = {@JsonCreator})
public class AIEvent extends GenericEventMsg {
    private AIActions action;
    private String auctionId;
    @Setter
    private String role;
    @Setter
    private String content;
    @Setter
    private boolean reasoning;

    private AIEvent(UUID eventId, AIActions action, String auctionId, String role, String content, boolean reasoning) {
        super(eventId);
        this.auctionId = auctionId;
        this.action = action;
        this.role = role;
        this.content = content;
        this.reasoning = reasoning;
    }

    public static AIEvent createEventMsg(AIActions action, String auctionId, String role, String content, boolean reasoning) {
        return new AIEvent(UUID.randomUUID(), action, auctionId, role, content, reasoning);
    }
    public static AIEvent createEventMsg(AIActions action, String auctionId) {
        return new AIEvent(UUID.randomUUID(), action, auctionId,"", "", false);
    }
}
