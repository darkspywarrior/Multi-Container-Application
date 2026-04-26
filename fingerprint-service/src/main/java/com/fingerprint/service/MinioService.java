package com.fingerprint.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {

        private static final String BUCKET = "fingerprints";

        private final MinioClient minioClient;

        public MinioService() {

                this.minioClient = MinioClient.builder()
                                .endpoint("http://minio:9000")
                                .credentials("admin", "password123")
                                .build();
        }

        public String uploadFile(MultipartFile file) throws Exception {

                createBucketIfMissing();

                // unique object name so uploads do not overwrite each other
                String objectName = System.currentTimeMillis()
                                + "-"
                                + file.getOriginalFilename();

                minioClient.putObject(
                                PutObjectArgs.builder()
                                                .bucket(BUCKET)
                                                .object(objectName)
                                                .stream(
                                                                file.getInputStream(),
                                                                file.getSize(),
                                                                -1)
                                                .contentType(
                                                                file.getContentType() != null
                                                                                ? file.getContentType()
                                                                                : "application/octet-stream")
                                                .build());

                System.out.println("Uploaded to MinIO: " + objectName);

                return objectName;
        }

        private void createBucketIfMissing() throws Exception {

                boolean exists = minioClient.bucketExists(
                                BucketExistsArgs.builder()
                                                .bucket(BUCKET)
                                                .build());

                if (!exists) {

                        minioClient.makeBucket(
                                        MakeBucketArgs.builder()
                                                        .bucket(BUCKET)
                                                        .build());

                        System.out.println("Created bucket: " + BUCKET);
                }
        }
}