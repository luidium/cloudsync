package com.luidium.cloudsync.controller;

import com.luidium.cloudsync.service.EventProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventProcessingService eventProcessingService;

    @Autowired
    public EventController(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @PostMapping
    public ResponseEntity<String> handleMinioEvent(@RequestBody String eventPayload) {
        eventProcessingService.processEvent(eventPayload);

        return ResponseEntity.ok("Event received");
    }
}
