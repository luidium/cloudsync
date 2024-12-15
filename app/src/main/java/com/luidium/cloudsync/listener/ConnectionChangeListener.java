package com.luidium.cloudsync.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.luidium.cloudsync.event.ConnectionChangedEvent;
import com.luidium.cloudsync.runner.FileWatcherRunner;

@Component
public class ConnectionChangeListener {

    private final FileWatcherRunner fileWatcherRunner;

    public ConnectionChangeListener(FileWatcherRunner fileWatcherRunner) {
        this.fileWatcherRunner = fileWatcherRunner;
    }

    @EventListener
    public void handleConnectionChanged(ConnectionChangedEvent event) {
        fileWatcherRunner.updateConnection(event.getConnectionId());
    }
}
