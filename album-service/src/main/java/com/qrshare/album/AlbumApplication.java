package com.qpic.album;

import com.qpic.common.client.MediaClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = MediaClient.class)
public class AlbumApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlbumApplication.class, args);
    }
}
