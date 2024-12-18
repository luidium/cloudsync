package com.luidium.cloudsync.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public class FileWatcherService {

    private final MinioClient minioClient;
    private final String bucketName;

    public FileWatcherService(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    private void registerAllDirs(Path start, WatchService watchService) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                );
                System.out.printf("Watching directory: %s%n", dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void watchDirectory(Path path) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            registerAllDirs(path, watchService);

            System.out.printf("Watching directory: %s for bucket: %s%n", path, bucketName);

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = path.resolve((Path) event.context());

                    System.out.printf("Event kind: %s. File: %s%n", kind, filePath);

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        uploadToMinio(filePath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        deleteFromMinio(filePath);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("WatchKey no longer valid, exiting...");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error watching directory: " + e.getMessage());
        }
    }

    private void uploadToMinio(Path filePath) {
        try {
            System.out.printf("Uploading file to Minio (Bucket: %s): %s%n", bucketName, filePath);

            ensureBucketExists();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath.getFileName().toString())
                    .stream(Files.newInputStream(filePath), Files.size(filePath), -1)
                    .contentType(Files.probeContentType(filePath))
                    .headers(Map.of("x-amz-meta-origin", "cloudsync"))
                    .build()
            );

            System.out.println("File uploaded successfully.");
        } catch (Exception e) {
            System.err.printf("Failed to upload file %s to bucket %s: %s%n", filePath, bucketName, e.getMessage());
        }
    }

    private void deleteFromMinio(Path filePath) {
        try {
            System.out.printf("Deleting file from Minio (Bucket: %s): %s%n", bucketName, filePath);

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath.getFileName().toString())
                    .build()
            );

            System.out.println("File deleted successfully.");
        } catch (Exception e) {
            System.err.printf("Failed to delete file %s from bucket %s: %s%n", filePath, bucketName, e.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                System.out.printf("Bucket created: %s%n", bucketName);
            }
        } catch (Exception e) {
            System.err.printf("Failed to ensure bucket %s exists: %s%n", bucketName, e.getMessage());
        }
    }
}
