package com.luidium.cloudsync.runner;

import com.luidium.cloudsync.model.ConnectionEntity;
import com.luidium.cloudsync.repository.ConnectionRepository;
import com.luidium.cloudsync.service.FileWatcherService;
import io.minio.MinioClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileWatcherRunner implements CommandLineRunner {

    private final MinioClient minioClient;
    private final ConnectionRepository connectionRepository;
    private final ConcurrentHashMap<Long, Thread> activeThreads = new ConcurrentHashMap<>();

    public FileWatcherRunner(MinioClient minioClient, ConnectionRepository connectionRepository) {
        this.minioClient = minioClient;
        this.connectionRepository = connectionRepository;
    }

    private void startSyncThread(ConnectionEntity connection) {
        Path directoryToWatch = Paths.get(connection.getDirectoryPath());
        String bucketName = connection.getBucketName();

        FileWatcherService watcherService = new FileWatcherService(minioClient, bucketName);
        Thread thread = new Thread(() -> watcherService.watchDirectory(directoryToWatch));
        thread.start();

        activeThreads.put(connection.getId(), thread);
        System.out.printf("Started syncing directory %s with bucket %s%n", directoryToWatch, bucketName);
    }

    private void stopSyncThread(Long connectionId) {
        Thread thread = activeThreads.remove(connectionId);
        if (thread != null) {
            thread.interrupt();
            System.out.printf("Stopped syncing for connection ID %d%n", connectionId);
        }
    }


    @Override
    public void run(String... args) {
        List<ConnectionEntity> activeConnections = connectionRepository.findByIsActive(true);

        for (ConnectionEntity connection : activeConnections) {
            Path directoryToWatch = Paths.get(connection.getDirectoryPath());
            String bucketName = connection.getBucketName();

            FileWatcherService watcherService = new FileWatcherService(minioClient, bucketName);
            new Thread(() -> watcherService.watchDirectory(directoryToWatch)).start();

            System.out.printf("Started syncing directory %s with bucket %s%n", directoryToWatch, bucketName);
        }
    }

    public void updateConnection(Long connectionId) {
        ConnectionEntity connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        if (connection.isActive()) {
            startSyncThread(connection);
        } else {
            stopSyncThread(connectionId);
        }
    }
}
