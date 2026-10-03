package com.qpic.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public record MediaProperties(long maxImageBytes, long maxVideoBytes, int viewUrlTtlMinutes, int uploadUrlTtlMinutes) {
}
