package com.qpic.album.service;

import com.qpic.album.domain.Album;
import com.qpic.album.domain.MediaGroup;
import com.qpic.album.domain.MediaGroupItem;
import com.qpic.album.repo.AlbumRepository;
import com.qpic.album.repo.MediaGroupRepository;
import com.qpic.album.web.AlbumDtos.AddItemsRequest;
import com.qpic.album.web.AlbumDtos.AlbumRequest;
import com.qpic.album.web.AlbumDtos.AlbumSummary;
import com.qpic.album.web.AlbumDtos.GroupRequest;
import com.qpic.album.web.AlbumDtos.GroupUpdateRequest;
import com.qpic.album.web.AlbumDtos.ItemUpdateRequest;
import com.qpic.common.client.MediaClient;
import com.qpic.common.dto.AlbumView;
import com.qpic.common.dto.MediaGroupView;
import com.qpic.common.dto.MediaItemView;
import com.qpic.common.dto.MediaView;
import com.qpic.common.dto.OwnershipCheck;
import com.qpic.common.dto.PageResponse;
import com.qpic.common.error.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private static final int MAX_GROUPS_PER_ALBUM = 200;
    private static final int MAX_ITEMS_PER_GROUP = 500;
    private static final int MEDIA_BATCH_SIZE = 200;

    private final AlbumRepository albums;
    private final MediaGroupRepository groupRepo;
    private final MediaClient mediaClient;

    // =================================================================== albums

    @Transactional
    public AlbumView create(UUID owner, AlbumRequest req) {
        Album a = new Album();
        a.setOwnerId(owner);
        applyAlbum(owner, a, req);
        albums.save(a);
        return toView(a);
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumSummary> list(UUID owner, Pageable pageable) {
        Page<Album> page = albums.findByOwnerId(owner, pageable);
        Set<UUID> coverIds = page.getContent().stream()
                .map(Album::getCoverMediaId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, MediaView> covers = fetchMedia(coverIds);
        return PageResponse.of(page, a -> new AlbumSummary(
                a.getId(), a.getTitle(), a.getDescription(),
                a.getCoverMediaId() == null ? null : covers.get(a.getCoverMediaId()),
                a.getGroups().size(),
                a.getGroups().stream().mapToInt(g -> g.getItems().size()).sum(),
                a.getCreatedAt(), a.getUpdatedAt()));
    }

    @Transactional(readOnly = true)
    public AlbumView get(UUID owner, UUID albumId) {
        return toView(loadOwned(owner, albumId));
    }

    @Transactional
    public AlbumView update(UUID owner, UUID albumId, AlbumRequest req) {
        Album a = loadOwned(owner, albumId);
        applyAlbum(owner, a, req);
        a.touch();
        return toView(a);
    }

    @Transactional
    public void delete(UUID owner, UUID albumId) {
        albums.delete(loadOwned(owner, albumId));
    }

    // =================================================================== groups

    @Transactional
    public MediaGroupView addGroup(UUID owner, UUID albumId, GroupRequest req) {
        Album a = loadOwned(owner, albumId);
        if (a.getGroups().size() >= MAX_GROUPS_PER_ALBUM) {
            throw ApiException.badRequest("An album can hold at most " + MAX_GROUPS_PER_ALBUM + " media groups");
        }
        MediaGroup g = new MediaGroup();
        g.setAlbum(a);
        g.setTitle(req.title().trim());
        g.setDescription(blankToNull(req.description()));
        g.setSortOrder(a.getGroups().stream().mapToInt(MediaGroup::getSortOrder).max().orElse(-1) + 1);
        a.getGroups().add(g);
        if (req.mediaIds() != null && !req.mediaIds().isEmpty()) {
            addItems(owner, g, req.mediaIds());
        }
        a.touch();
        groupRepo.saveAndFlush(g);
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional(readOnly = true)
    public MediaGroupView getGroup(UUID owner, UUID albumId, UUID groupId) {
        MediaGroup g = loadGroup(loadOwned(owner, albumId), groupId);
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional
    public MediaGroupView updateGroup(UUID owner, UUID albumId, UUID groupId, GroupUpdateRequest req) {
        Album a = loadOwned(owner, albumId);
        MediaGroup g = loadGroup(a, groupId);
        g.setTitle(req.title().trim());
        g.setDescription(blankToNull(req.description()));
        a.touch();
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional
    public void deleteGroup(UUID owner, UUID albumId, UUID groupId) {
        Album a = loadOwned(owner, albumId);
        MediaGroup g = loadGroup(a, groupId);
        a.getGroups().remove(g);
        a.touch();
    }

    @Transactional
    public AlbumView reorderGroups(UUID owner, UUID albumId, List<UUID> orderedIds) {
        Album a = loadOwned(owner, albumId);
        applyOrder(a.getGroups(), orderedIds, MediaGroup::getId, MediaGroup::setSortOrder);
        a.touch();
        return toView(a);
    }

    // =================================================================== items

    @Transactional
    public MediaGroupView addItems(UUID owner, UUID albumId, UUID groupId, AddItemsRequest req) {
        Album a = loadOwned(owner, albumId);
        MediaGroup g = loadGroup(a, groupId);
        addItems(owner, g, req.mediaIds());
        groupRepo.saveAndFlush(g);
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional
    public MediaGroupView updateItem(UUID owner, UUID albumId, UUID groupId, UUID itemId, ItemUpdateRequest req) {
        MediaGroup g = loadGroup(loadOwned(owner, albumId), groupId);
        MediaGroupItem item = g.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Item not found"));
        item.setCaption(blankToNull(req.caption()));
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional
    public MediaGroupView removeItem(UUID owner, UUID albumId, UUID groupId, UUID itemId) {
        Album a = loadOwned(owner, albumId);
        MediaGroup g = loadGroup(a, groupId);
        boolean removed = g.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw ApiException.notFound("Item not found");
        }
        a.touch();
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional
    public MediaGroupView reorderItems(UUID owner, UUID albumId, UUID groupId, List<UUID> orderedItemIds) {
        Album a = loadOwned(owner, albumId);
        MediaGroup g = loadGroup(a, groupId);
        applyOrder(g.getItems(), orderedItemIds, MediaGroupItem::getId, MediaGroupItem::setSortOrder);
        a.touch();
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    // =================================================================== internal (service-to-service)

    @Transactional(readOnly = true)
    public AlbumView internalAlbum(UUID albumId) {
        return toView(albums.findById(albumId).orElseThrow(() -> ApiException.notFound("Album not found")));
    }

    @Transactional(readOnly = true)
    public UUID albumOwner(UUID albumId) {
        return albums.findById(albumId).map(Album::getOwnerId).orElseThrow(() -> ApiException.notFound("Album not found"));
    }

    @Transactional(readOnly = true)
    public MediaGroupView internalGroup(UUID groupId) {
        MediaGroup g = groupRepo.findById(groupId).orElseThrow(() -> ApiException.notFound("Media group not found"));
        return groupView(g, fetchMedia(mediaIdsOf(g)));
    }

    @Transactional(readOnly = true)
    public UUID groupOwner(UUID groupId) {
        return groupRepo.findById(groupId).map(g -> g.getAlbum().getOwnerId())
                .orElseThrow(() -> ApiException.notFound("Media group not found"));
    }

    // =================================================================== helpers

    private void applyAlbum(UUID owner, Album a, AlbumRequest req) {
        a.setTitle(req.title().trim());
        a.setDescription(blankToNull(req.description()));
        if (req.coverMediaId() != null) {
            requireOwnedMedia(owner, List.of(req.coverMediaId()));
            a.setCoverMediaId(req.coverMediaId());
        }
    }

    private void addItems(UUID owner, MediaGroup g, List<UUID> mediaIds) {
        List<UUID> distinct = mediaIds.stream().distinct().toList();
        requireOwnedMedia(owner, distinct);

        Set<UUID> existing = g.getItems().stream().map(MediaGroupItem::getMediaId).collect(Collectors.toSet());
        List<UUID> toAdd = distinct.stream().filter(id -> !existing.contains(id)).toList();
        if (g.getItems().size() + toAdd.size() > MAX_ITEMS_PER_GROUP) {
            throw ApiException.badRequest("A media group can hold at most " + MAX_ITEMS_PER_GROUP + " items");
        }

        int next = g.getItems().stream().mapToInt(MediaGroupItem::getSortOrder).max().orElse(-1) + 1;
        for (UUID mediaId : toAdd) {
            MediaGroupItem item = new MediaGroupItem();
            item.setGroup(g);
            item.setMediaId(mediaId);
            item.setSortOrder(next++);
            g.getItems().add(item);
        }

        Album a = g.getAlbum();
        if (a.getCoverMediaId() == null && !toAdd.isEmpty()) {
            a.setCoverMediaId(toAdd.get(0));   // first media becomes the album cover
        }
        a.touch();
    }

    private void requireOwnedMedia(UUID owner, List<UUID> mediaIds) {
        List<UUID> distinct = mediaIds.stream().distinct().toList();
        List<UUID> ok = mediaClient.owned(new OwnershipCheck(owner, distinct));
        if (new HashSet<>(ok).size() != distinct.size()) {
            throw ApiException.badRequest("Some media do not exist, are not finished uploading, or are not yours");
        }
    }

    private Album loadOwned(UUID owner, UUID albumId) {
        return albums.findById(albumId)
                .filter(a -> a.getOwnerId().equals(owner))
                .orElseThrow(() -> ApiException.notFound("Album not found"));
    }

    private MediaGroup loadGroup(Album a, UUID groupId) {
        return a.getGroups().stream().filter(g -> g.getId().equals(groupId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Media group not found"));
    }

    private <T> void applyOrder(Collection<T> current, List<UUID> ids, Function<T, UUID> idOf, BiConsumer<T, Integer> setter) {
        Map<UUID, T> byId = current.stream().collect(Collectors.toMap(idOf, Function.identity()));
        if (ids.size() != byId.size() || !byId.keySet().equals(new HashSet<>(ids))) {
            throw ApiException.badRequest("Order must contain every existing id exactly once");
        }
        for (int i = 0; i < ids.size(); i++) {
            setter.accept(byId.get(ids.get(i)), i);
        }
    }

    // ---- view building (resolves media through media-service in batches) ----

    private AlbumView toView(Album a) {
        Set<UUID> ids = new LinkedHashSet<>();
        if (a.getCoverMediaId() != null) {
            ids.add(a.getCoverMediaId());
        }
        a.getGroups().forEach(g -> ids.addAll(mediaIdsOf(g)));
        Map<UUID, MediaView> media = fetchMedia(ids);

        List<MediaGroupView> groups = a.getGroups().stream()
                .sorted(Comparator.comparingInt(MediaGroup::getSortOrder))
                .map(g -> groupView(g, media))
                .toList();
        MediaView cover = a.getCoverMediaId() == null ? null : media.get(a.getCoverMediaId());
        return new AlbumView(a.getId(), a.getOwnerId(), a.getTitle(), a.getDescription(), cover,
                a.getCreatedAt(), a.getUpdatedAt(), groups);
    }

    private MediaGroupView groupView(MediaGroup g, Map<UUID, MediaView> media) {
        List<MediaItemView> items = g.getItems().stream()
                .sorted(Comparator.comparingInt(MediaGroupItem::getSortOrder))
                .filter(i -> media.containsKey(i.getMediaId()))     // media deleted meanwhile -> skip
                .map(i -> new MediaItemView(i.getId(), i.getSortOrder(), i.getCaption(), media.get(i.getMediaId())))
                .toList();
        return new MediaGroupView(g.getId(), g.getAlbum().getId(), g.getAlbum().getOwnerId(),
                g.getTitle(), g.getDescription(), g.getSortOrder(), items);
    }

    private static Set<UUID> mediaIdsOf(MediaGroup g) {
        return g.getItems().stream().map(MediaGroupItem::getMediaId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<UUID, MediaView> fetchMedia(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<UUID> all = new ArrayList<>(new LinkedHashSet<>(ids));
        Map<UUID, MediaView> result = new HashMap<>();
        for (int i = 0; i < all.size(); i += MEDIA_BATCH_SIZE) {
            List<UUID> chunk = all.subList(i, Math.min(i + MEDIA_BATCH_SIZE, all.size()));
            mediaClient.batch(chunk).forEach(m -> result.put(m.id(), m));
        }
        return result;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
