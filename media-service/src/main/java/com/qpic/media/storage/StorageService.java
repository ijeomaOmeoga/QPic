package com.qpic.media.storage;

import com.qpic.media.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.util.OptionalLong;

/** Thin wrapper around MinIO / any S3-compatible store. Swap for the AWS SDK without touching callers. */
@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final MinioClient internal;
    private final MinioClient publicClient;
    private final MinioProperties props;

    public StorageService(@Qualifier("internalMinioClient") MinioClient internal,
                          @Qualifier("publicMinioClient") MinioClient publicClient,
                          MinioProperties props) {
        this.internal = internal;
        this.publicClient = publicClient;
        this.props = props;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucket() {
        try {
            if (!internal.bucketExists(BucketExistsArgs.builder().bucket(props.bucket()).build())) {
                internal.makeBucket(MakeBucketArgs.builder().bucket(props.bucket()).region(props.region()).build());
                log.info("Created bucket {}", props.bucket());
            }
        } catch (Exception e) {
            log.warn("Could not verify/create bucket '{}': {}", props.bucket(), e.getMessage());
        }
    }

    public void put(String key, InputStream in, long size, String contentType) {
        try {
            internal.putObject(PutObjectArgs.builder()
                    .bucket(props.bucket()).object(key)
                    .stream(in, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store object " + key, e);
        }
    }

    public String presignGet(String key, Duration ttl) {
        return presign(Method.GET, key, ttl);
    }

    public String presignPut(String key, Duration ttl) {
        return presign(Method.PUT, key, ttl);
    }

    public OptionalLong size(String key) {
        try {
            StatObjectResponse stat = internal.statObject(StatObjectArgs.builder().bucket(props.bucket()).object(key).build());
            return OptionalLong.of(stat.size());
        } catch (ErrorResponseException e) {
            String code = e.errorResponse() != null ? e.errorResponse().code() : "";
            if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code) || "NotFound".equals(code)) {
                return OptionalLong.empty();
            }
            throw new IllegalStateException("Failed to stat object " + key, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stat object " + key, e);
        }
    }

    public void delete(String key) {
        try {
            internal.removeObject(RemoveObjectArgs.builder().bucket(props.bucket()).object(key).build());
        } catch (Exception e) {
            log.warn("Failed to delete object {}: {}", key, e.getMessage());
        }
    }

    private String presign(Method method, String key, Duration ttl) {
        try {
            return publicClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(props.bucket())
                    .object(key)
                    .expiry((int) ttl.toSeconds())
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to presign " + key, e);
        }
    }
}
