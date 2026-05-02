package com.fingerprint.service;

import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {

        private final MinioClient minioClient;

        public MinioService() {

                String accessKey = System.getenv("MINIO_ACCESS_KEY");
                String secretKey = System.getenv("MINIO_SECRET_KEY");
                String endpoint = System.getenv("MINIO_ENDPOINT");

                this.minioClient = MinioClient.builder()
                                .endpoint(endpoint)
                                .credentials(accessKey, secretKey)
                                .build();
        }

        public void uploadFile(MultipartFile file) throws Exception {

                boolean exists = minioClient.bucketExists(
                                BucketExistsArgs.builder()
                                                .bucket("fingerprints")
                                                .build());

                if (!exists) {
                        minioClient.makeBucket(
                                        MakeBucketArgs.builder()
                                                        .bucket("fingerprints")
                                                        .build());
                }

                minioClient.putObject(
                                PutObjectArgs.builder()
                                                .bucket("fingerprints")
                                                .object(file.getOriginalFilename())
                                                .stream(
                                                                file.getInputStream(),
                                                                file.getSize(),
                                                                -1)
                                                .build());

                System.out.println("✅ Uploaded to MinIO: " + file.getOriginalFilename());
        }
}