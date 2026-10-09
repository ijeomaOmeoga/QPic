package com.qpic.common.client;

import com.qpic.common.dto.MediaView;
import com.qpic.common.dto.OwnerResponse;
import com.qpic.common.dto.OwnershipCheck;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "media-service", contextId = "mediaClient", path = "/internal/media")
public interface MediaClient {

    /** READY media for the given ids (unknown / pending ids are simply absent) with fresh presigned URLs. */
    @PostMapping("/batch")
    List<MediaView> batch(@RequestBody List<UUID> ids);

    /** Returns the subset of ids that are READY and owned by ownerId. */
    @PostMapping("/owned")
    List<UUID> owned(@RequestBody OwnershipCheck check);

    @GetMapping("/{id}")
    MediaView get(@PathVariable("id") UUID id);

    @GetMapping("/{id}/owner")
    OwnerResponse owner(@PathVariable("id") UUID id);
}
