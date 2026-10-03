package com.qpic.media.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    /** Used for put / stat / delete from inside the cluster. */
    @Bean("internalMinioClient")
    public MinioClient internalMinioClient(MinioProperties p) {
        return MinioClient.builder()
                .endpoint(p.endpoint())
                .credentials(p.accessKey(), p.secretKey())
                .region(p.region())
                .build();
    }

    /** Only used to SIGN urls for the public host. Region is fixed so signing never needs a network call. */
    @Bean("publicMinioClient")
    public MinioClient publicMinioClient(MinioProperties p) {
        return MinioClient.builder()
                .endpoint(p.publicEndpoint())
                .credentials(p.accessKey(), p.secretKey())
                .region(p.region())
                .build();
    }
}
