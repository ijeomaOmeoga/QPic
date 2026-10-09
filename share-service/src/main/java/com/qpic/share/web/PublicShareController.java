package com.qpic.share.web;

import com.qpic.share.service.QrCodeService;
import com.qpic.share.service.ShareService;
import com.qpic.share.web.ShareDtos.ResolveRequest;
import com.qpic.share.web.ShareDtos.ShareContent;
import com.qpic.share.web.ShareDtos.ShareInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * No login required (the gateway lets /api/public/** through). Access is granted by knowing the token.
 * Flow in the app:  scan QR -> GET info -> (ask password if needed) -> POST resolve -> render content.
 */
@RestController
@RequestMapping("/api/public/shares")
@RequiredArgsConstructor
public class PublicShareController {

    private final ShareService service;
    private final QrCodeService qr;

    @GetMapping("/{token}/info")
    public ShareInfo info(@PathVariable String token) {
        return service.info(token);
    }

    @PostMapping("/{token}/resolve")
    public ShareContent resolve(@PathVariable String token,
                                @Valid @RequestBody(required = false) ResolveRequest body,
                                HttpServletRequest request) {
        return service.resolve(token, body == null ? null : body.password(), clientIp(request),
                request.getHeader(HttpHeaders.USER_AGENT));
    }

    @GetMapping(value = "/{token}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable String token, @RequestParam(defaultValue = "512") int size) {
        byte[] png = qr.png(service.qrContentForToken(token), size);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic()).body(png);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
