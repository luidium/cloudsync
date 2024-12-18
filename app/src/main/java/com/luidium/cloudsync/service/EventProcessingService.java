package com.luidium.cloudsync.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luidium.cloudsync.model.ConnectionEntity;
import com.luidium.cloudsync.repository.ConnectionRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class EventProcessingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConnectionRepository connectionRepository;
    private final MinioClientService minioClientService;

    public EventProcessingService(ConnectionRepository connectionRepository, MinioClientService minioClientService) {
        this.connectionRepository = connectionRepository;
        this.minioClientService = minioClientService;
    }

    public void processEvent(String eventPayload) {
        try {
            JsonNode root = objectMapper.readTree(eventPayload);
            JsonNode records = root.get("Records");

            if (records != null) {
                for (JsonNode record : records) {
                    String eventName = record.get("eventName").asText();
                    String bucketName = record.get("s3").get("bucket").get("name").asText();
                    String objectKey = record.get("s3").get("object").get("key").asText();

                    System.out.printf("Event: %s, Bucket: %s, Key: %s%n", eventName, bucketName, objectKey);

                    // 동기화 작업
                    handleEvent(eventName, bucketName, objectKey);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing Minio event: " + e.getMessage());
        }
    }

    private void handleEvent(String eventName, String bucketName, String objectKey) {
        ConnectionEntity connection = connectionRepository.findByBucketName(bucketName)
                .orElseThrow(() -> new RuntimeException("No directory mapped to bucket: " + bucketName));

        if (!connection.isActive()) {
            System.out.printf("Ignoring event for inactive connection: %s/%s%n", bucketName, objectKey);
            return;
        }

        Path localDirectory = Paths.get(connection.getDirectoryPath());
        Path localFilePath = localDirectory.resolve(objectKey);

        try {
            Map<String, String> metadata = minioClientService.getObjectMetadata(bucketName, objectKey);

            if ("cloudsync".equals(metadata.get("origin"))) {
                System.out.printf("Ignoring event for file uploaded by CloudSync: %s/%s%n", bucketName, objectKey);
                return;
            }
        } catch (Exception e) {
            System.err.printf("Error getting object metadata: %s/%s. Details: %s%n", bucketName, objectKey, e.getMessage());
        }

        try {
            if (eventName.contains("s3:ObjectCreated")) {
                System.out.printf("File created: %s/%s%n", bucketName, objectKey);

                downloadFromMinio(bucketName, objectKey, localFilePath);
            } else if (eventName.contains("s3:ObjectRemoved")) {
                System.out.printf("File deleted: %s/%s%n", bucketName, objectKey);

                deleteLocalFile(localFilePath);
            }
        } catch (Exception e) {
            System.err.printf("Error handling event: %s. Details: %s%n", eventName, e.getMessage());
        }
    }

    private void downloadFromMinio(String bucketName, String objectKey, Path localFilePath) throws Exception {
        InputStream inputStream = minioClientService.getObject(bucketName, objectKey);

        Files.createDirectories(localFilePath.getParent());

        try (FileOutputStream outputStream = new FileOutputStream(localFilePath.toFile())) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        System.out.printf("Downloaded file: %s%n", localFilePath);
    }

    private void deleteLocalFile(Path localFilePath) throws Exception {
        File file = localFilePath.toFile();

        if (file.exists()) {
            if (file.delete()) {
                System.out.printf("Deleted local file: %s%n", localFilePath);
            } else {
                System.err.printf("Failed to delete local file: %s%n", localFilePath);
            }
        } else {
            System.out.printf("Local file does not exist, skipping deletion: %s%n", localFilePath);
        }
    }
}
