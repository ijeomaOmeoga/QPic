package com.qrshare.media.web;

import com.qrshare.common.dto.MediaView;
import com.qrshare.common.dto.OwnerResponse;
import com.qrshare.common.dto.OwnershipCheck;
import com.qrshare.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Service-to-service only. Never exposed by the gateway; guarded by InternalApiFilter. */
@RestController
@RequestMapping("/internal/media")
@RequiredArgsConstructor
public class InternalMediaController {

    private final MediaService service;

    @PostMapping("/batch")
    public List<MediaView> batch(@RequestBody List<UUID> ids) {
        return service.batch(ids);
    }

    @PostMapping("/owned")
    public List<UUID> owned(@RequestBody OwnershipCheck check) {
        return service.ownedReady(check.ownerId(), check.mediaIds());
    }

    @GetMapping("/{id}")
    public MediaView get(@PathVariable UUID id) {
        return service.internalGet(id);
    }

    @GetMapping("/{id}/owner")
    public OwnerResponse owner(@PathVariable UUID id) {
        return new OwnerResponse(service.ownerOf(id));
    }
}
