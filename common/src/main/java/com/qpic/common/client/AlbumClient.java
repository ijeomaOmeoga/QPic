package com.qpic.common.client;

import com.qpic.common.dto.AlbumView;
import com.qpic.common.dto.MediaGroupView;
import com.qpic.common.dto.OwnerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "album-service", contextId = "albumClient", path = "/internal")
public interface AlbumClient {

    @GetMapping("/albums/{id}")
    AlbumView album(@PathVariable("id") UUID id);

    @GetMapping("/albums/{id}/owner")
    OwnerResponse albumOwner(@PathVariable("id") UUID id);

    @GetMapping("/groups/{id}")
    MediaGroupView group(@PathVariable("id") UUID id);

    @GetMapping("/groups/{id}/owner")
    OwnerResponse groupOwner(@PathVariable("id") UUID id);
}
