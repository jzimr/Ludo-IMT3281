package no.ntnu.imt3281.ludo.logic.messages;

import java.util.UUID;

public class ClientSessionID extends Message {
    private final UUID session;

    public ClientSessionID(String action){
        super(action);
        session = UUID.randomUUID();
    }

    public UUID getSession() {
        return session;
    }
}
