package com.luidium.cloudsync.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketNotificationArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.EventType;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.QueueConfiguration;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

@Service
public class MinioClientService {

    private final MinioClient minioClient;

    public MinioClientService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public InputStream getObject(String bucketName, String objectKey) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build()
        );
    }

    public Map<String, String> getObjectMetadata(String bucketName, String objectKey) throws Exception {
        StatObjectResponse res = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build()
        );
        
        return res.userMetadata();
    }

    public void createBucketWithNotification(String bucketName) throws Exception {
        if (minioClient.bucketExists(
            BucketExistsArgs.builder()
            .bucket(bucketName)
            .build()
        )) {
            throw new RuntimeException("Bucket already exists");
        }

        minioClient.makeBucket(
            MakeBucketArgs.builder()
            .bucket(bucketName)
            .build()
        );

        QueueConfiguration queueConfig = new QueueConfiguration();
        queueConfig.setQueue("arn:minio:sqs::cloudsync:webhook");
        queueConfig.setEvents(
            Arrays.asList(EventType.OBJECT_CREATED_PUT, EventType.OBJECT_REMOVED_DELETE)
        );

        NotificationConfiguration notificationConfig = new NotificationConfiguration();
        notificationConfig.setQueueConfigurationList(
            Arrays.asList(queueConfig)
        );

        minioClient.setBucketNotification(
            SetBucketNotificationArgs.builder()
            .bucket(bucketName)
            .config(notificationConfig)
            .build()
        );

        System.out.printf("Created bucket %s with notification configuration%n", bucketName);
    }
}
