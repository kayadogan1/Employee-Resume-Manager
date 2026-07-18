package com.employeeresumemaneger.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() throws Exception {
        log.info("[MINIO-SERVICE] Bucket kontrol ediliyor: {}", bucketName);
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("[MINIO-SERVICE] Bucket oluşturuldu: {}", bucketName);
        } else {
            log.info("[MINIO-SERVICE] Bucket zaten mevcut: {}", bucketName);
        }
    }

    public void upload(MultipartFile file, String storageKey) throws Exception {
        log.info("[MINIO-SERVICE] Dosya yükleniyor -> storageKey: {}, boyut: {} bytes, tür: {}",
                storageKey, file.getSize(), file.getContentType());
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        }
        log.info("[MINIO-SERVICE] Dosya başarıyla yüklendi -> storageKey: {}", storageKey);
    }

    public InputStream download(String storageKey) throws Exception {
        log.info("[MINIO-SERVICE] Dosya indiriliyor -> storageKey: {}", storageKey);
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storageKey)
                        .build());
    }

    public void delete(String storageKey) throws Exception {
        log.info("[MINIO-SERVICE] Dosya siliniyor -> storageKey: {}", storageKey);
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storageKey)
                        .build());
        log.info("[MINIO-SERVICE] Dosya başarıyla silindi -> storageKey: {}", storageKey);
    }

    public String getPresignedUrl(String storageKey) throws Exception {
        log.info("[MINIO-SERVICE] Presigned URL oluşturuluyor -> storageKey: {}", storageKey);
        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(storageKey)
                        .expiry(10, TimeUnit.MINUTES)
                        .build());
        log.info("[MINIO-SERVICE] Presigned URL oluşturuldu -> {}", url);
        return url;
    }
}
