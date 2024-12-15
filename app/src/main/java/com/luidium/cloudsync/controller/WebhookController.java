package com.luidium.cloudsync.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luidium.cloudsync.service.EventProcessingService;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final EventProcessingService eventProcessingService;

    public WebhookController(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String eventPayload) {
        CompletableFuture.runAsync(() -> eventProcessingService.processEvent(eventPayload));

        return ResponseEntity.ok("Event processing started");
    }
}
