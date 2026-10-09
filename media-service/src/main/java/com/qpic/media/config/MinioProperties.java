package com.qpic.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * endpoint        = how THIS service reaches MinIO/S3 (docker network name)
 * publicEndpoint  = how the PHONE reaches it; presigned URLs are signed for this host
 */
@ConfigurationProperties(prefix = "app.minio")
public record MinioProperties(String endpoint, String publicEndpoint, String accessKey, String secretKey,
                              String bucket, String region) {
}
