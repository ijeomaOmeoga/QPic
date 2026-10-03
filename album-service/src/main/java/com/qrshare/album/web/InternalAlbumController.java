package com.qrshare.album.web;

import com.qrshare.album.service.AlbumService;
import com.qrshare.common.dto.AlbumView;
import com.qrshare.common.dto.MediaGroupView;
import com.qrshare.common.dto.OwnerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Service-to-service only (used by share-service). Never routed by the gateway. */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAlbumController {

    private final AlbumService service;

    @GetMapping("/albums/{id}")
    public AlbumView album(@PathVariable UUID id) {
        return service.internalAlbum(id);
    }

    @GetMapping("/albums/{id}/owner")
    public OwnerResponse albumOwner(@PathVariable UUID id) {
        return new OwnerResponse(service.albumOwner(id));
    }

    @GetMapping("/groups/{id}")
    public MediaGroupView group(@PathVariable UUID id) {
        return service.internalGroup(id);
    }

    @GetMapping("/groups/{id}/owner")
    public OwnerResponse groupOwner(@PathVariable UUID id) {
        return new OwnerResponse(service.groupOwner(id));
    }
}
