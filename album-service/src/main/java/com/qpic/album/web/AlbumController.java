package com.are.album.web;

import com.are.album.service.AlbumService;
import com.are.album.web.AlbumDtos.AddItemsRequest;
import com.are.album.web.AlbumDtos.AlbumRequest;
import com.are.album.web.AlbumDtos.AlbumSummary;
import com.are.album.web.AlbumDtos.GroupRequest;
import com.are.album.web.AlbumDtos.GroupUpdateRequest;
import com.are.album.web.AlbumDtos.ItemUpdateRequest;
import com.are.album.web.AlbumDtos.ReorderRequest;
import com.are.common.dto.AlbumView;
import com.are.common.dto.MediaGroupView;
import com.are.common.dto.PageResponse;
import com.are.common.web.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService service;

    // ---------------- albums
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumView create(@CurrentUser UUID userId, @Valid @RequestBody AlbumRequest req) {
        return service.create(userId, req);
    }

    @GetMapping
    public PageResponse<AlbumSummary> list(@CurrentUser UUID userId,
                                           @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(userId, pageable);
    }

    @GetMapping("/{albumId}")
    public AlbumView get(@CurrentUser UUID userId, @PathVariable UUID albumId) {
        return service.get(userId, albumId);
    }

    @PutMapping("/{albumId}")
    public AlbumView update(@CurrentUser UUID userId, @PathVariable UUID albumId, @Valid @RequestBody AlbumRequest req) {
        return service.update(userId, albumId, req);
    }

    @DeleteMapping("/{albumId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser UUID userId, @PathVariable UUID albumId) {
        service.delete(userId, albumId);
    }

    // ---------------- media groups
    @PostMapping("/{albumId}/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaGroupView addGroup(@CurrentUser UUID userId, @PathVariable UUID albumId, @Valid @RequestBody GroupRequest req) {
        return service.addGroup(userId, albumId, req);
    }

    @PutMapping("/{albumId}/groups/order")
    public AlbumView reorderGroups(@CurrentUser UUID userId, @PathVariable UUID albumId, @Valid @RequestBody ReorderRequest req) {
        return service.reorderGroups(userId, albumId, req.ids());
    }

    @GetMapping("/{albumId}/groups/{groupId}")
    public MediaGroupView getGroup(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId) {
        return service.getGroup(userId, albumId, groupId);
    }

    @PutMapping("/{albumId}/groups/{groupId}")
    public MediaGroupView updateGroup(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId,
                                      @Valid @RequestBody GroupUpdateRequest req) {
        return service.updateGroup(userId, albumId, groupId, req);
    }

    @DeleteMapping("/{albumId}/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId) {
        service.deleteGroup(userId, albumId, groupId);
    }

    // ---------------- items (media inside a group)
    @PostMapping("/{albumId}/groups/{groupId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaGroupView addItems(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId,
                                   @Valid @RequestBody AddItemsRequest req) {
        return service.addItems(userId, albumId, groupId, req);
    }

    @PutMapping("/{albumId}/groups/{groupId}/items/order")
    public MediaGroupView reorderItems(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId,
                                       @Valid @RequestBody ReorderRequest req) {
        return service.reorderItems(userId, albumId, groupId, req.ids());
    }

    @PatchMapping("/{albumId}/groups/{groupId}/items/{itemId}")
    public MediaGroupView updateItem(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId,
                                     @PathVariable UUID itemId, @Valid @RequestBody ItemUpdateRequest req) {
        return service.updateItem(userId, albumId, groupId, itemId, req);
    }

    @DeleteMapping("/{albumId}/groups/{groupId}/items/{itemId}")
    public MediaGroupView removeItem(@CurrentUser UUID userId, @PathVariable UUID albumId, @PathVariable UUID groupId,
                                     @PathVariable UUID itemId) {
        return service.removeItem(userId, albumId, groupId, itemId);
    }
}
