package com.luidium.cloudsync.controller;

import com.luidium.cloudsync.event.ConnectionChangedEvent;
import com.luidium.cloudsync.model.ConnectionEntity;
import com.luidium.cloudsync.repository.ConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping
    public List<ConnectionEntity> getAllConnections() {
        return connectionRepository.findAll();
    }

    @PostMapping
    public ConnectionEntity createConnection(@RequestBody ConnectionEntity connection) {
        return connectionRepository.save(connection);
    }

    @PutMapping("/{id}/activate")
    public ConnectionEntity activateConnection(@PathVariable Long id) {
        ConnectionEntity connection = connectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Connection not found"));
        connection.setActive(true);
        connectionRepository.save(connection);

        eventPublisher.publishEvent(new ConnectionChangedEvent(id));
        return connection;
    }

    @PutMapping("/{id}/deactivate")
    public ConnectionEntity deactivateConnection(@PathVariable Long id) {
        ConnectionEntity connection = connectionRepository.findById(id).orElseThrow(() -> new RuntimeException("Connection not found"));
        connection.setActive(false);
        connectionRepository.save(connection);

        eventPublisher.publishEvent(new ConnectionChangedEvent(id));
        return connection;
    }

    @DeleteMapping("/{id}")
    public void deleteConnection(@PathVariable Long id) {
        connectionRepository.deleteById(id);
    }
}
