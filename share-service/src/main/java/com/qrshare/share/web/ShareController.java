package com.qpic.share.web;

import com.qpic.common.dto.PageResponse;
import com.qpic.common.web.CurrentUser;
import com.qpic.share.service.QrCodeService;
import com.qpic.share.service.ShareService;
import com.qpic.share.web.ShareDtos.CreateShareRequest;
import com.qpic.share.web.ShareDtos.ShareDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

/** Authenticated endpoints for the owner of the content. */
@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService service;
    private final QrCodeService qr;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShareDto create(@CurrentUser UUID userId, @Valid @RequestBody CreateShareRequest req) {
        return service.create(userId, req);
    }

    @GetMapping
    public PageResponse<ShareDto> list(@CurrentUser UUID userId,
                                       @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listMine(userId, pageable);
    }

    @GetMapping("/{id}")
    public ShareDto get(@CurrentUser UUID userId, @PathVariable UUID id) {
        return service.getMine(userId, id);
    }

    /** Revokes the link (QR stops working). The row is kept for audit/analytics. */
    @DeleteMapping("/{id}")
    public ShareDto revoke(@CurrentUser UUID userId, @PathVariable UUID id) {
        return service.revoke(userId, id);
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@CurrentUser UUID userId, @PathVariable UUID id,
                                     @RequestParam(defaultValue = "512") int size) {
        byte[] png = qr.png(service.qrContentFor(userId, id), size);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate()).body(png);
    }
}
