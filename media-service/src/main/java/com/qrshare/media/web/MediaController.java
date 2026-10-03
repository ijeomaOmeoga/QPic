package com.qrshare.media.web;

import com.qrshare.common.dto.MediaKind;
import com.qrshare.common.dto.MediaView;
import com.qrshare.common.dto.PageResponse;
import com.qrshare.common.web.CurrentUser;
import com.qrshare.media.service.MediaService;
import com.qrshare.media.web.MediaDtos.CompleteUploadRequest;
import com.qrshare.media.web.MediaDtos.InitUploadRequest;
import com.qrshare.media.web.MediaDtos.InitUploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService service;

    /** Simple multipart upload (good for images). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaView upload(@CurrentUser UUID userId,
                            @RequestPart("file") MultipartFile file,
                            @RequestParam(required = false) Integer width,
                            @RequestParam(required = false) Integer height,
                            @RequestParam(required = false) Long durationMs) {
        return service.upload(userId, file, width, height, durationMs);
    }

    /** Step 1 of direct-to-storage upload (recommended for video). */
    @PostMapping("/uploads")
    @ResponseStatus(HttpStatus.CREATED)
    public InitUploadResponse initUpload(@CurrentUser UUID userId, @Valid @RequestBody InitUploadRequest req) {
        return service.initUpload(userId, req);
    }

    /** Step 3: confirm the PUT to storage succeeded and attach metadata. */
    @PostMapping("/{id}/complete")
    public MediaView complete(@CurrentUser UUID userId, @PathVariable UUID id,
                              @RequestBody(required = false) CompleteUploadRequest req) {
        return service.completeUpload(userId, id, req);
    }

    @GetMapping
    public PageResponse<MediaView> list(@CurrentUser UUID userId,
                                        @RequestParam(required = false) MediaKind kind,
                                        @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(userId, kind, pageable);
    }

    @GetMapping("/{id}")
    public MediaView get(@CurrentUser UUID userId, @PathVariable UUID id) {
        return service.get(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser UUID userId, @PathVariable UUID id) {
        service.delete(userId, id);
    }
}
