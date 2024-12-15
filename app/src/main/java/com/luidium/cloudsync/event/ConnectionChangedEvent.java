package com.luidium.cloudsync.event;

public class ConnectionChangedEvent {
    private final Long connectionId;

    public ConnectionChangedEvent(Long connectionId) {
        this.connectionId = connectionId;
    }

    public Long getConnectionId() {
        return connectionId;
    }
}
