package com.qpic.share;

import com.qpic.common.client.AlbumClient;
import com.qpic.common.client.MediaClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(clients = {AlbumClient.class, MediaClient.class})
public class ShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShareApplication.class, args);
    }
}
