package com.qpic.share.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * baseUrl        - https link encoded in the QR (universal/app link domain, falls back to a web page)
 * deepLinkPrefix - custom scheme handled by the Expo app, e.g. qrshare://s/
 */
@ConfigurationProperties(prefix = "app.share")
public record ShareProperties(String baseUrl, String deepLinkPrefix) {
}
