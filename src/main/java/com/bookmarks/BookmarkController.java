package com.bookmarks;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getBookmarks(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "sort", required = false, defaultValue = "createdAt,desc") String sort,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");

        List<Bookmark> bookmarks;

        if (tags != null && !tags.isEmpty()) {
            // First we fetch matches for each tag and then intersect (or just match if one tag)
            // Let's perform query filtering. Note: standard requirement is basic search/filter by title OR tag.
            // But let's handle tags matching correctly.
            // Since BookmarkRepository already has: List<Bookmark> findAllByUserAndTagsNameIgnoreCaseOrderByCreatedAtDesc(User user, String tagName);
            // Let's use it for each tag or handle combination if multiple tags are specified.
            Set<Long> matchedIds = null;
            for (String tag : tags) {
                if (tag == null || tag.trim().isEmpty()) continue;
                List<Bookmark> list = bookmarkRepository.findAllByUserAndTagsNameIgnoreCaseOrderByCreatedAtDesc(user, tag.trim());
                Set<Long> ids = list.stream().map(Bookmark::getId).collect(Collectors.toSet());
                if (matchedIds == null) {
                    matchedIds = new HashSet<>(ids);
                } else {
                    matchedIds.retainAll(ids);
                }
            }
            if (matchedIds == null) {
                bookmarks = bookmarkRepository.findAllByUserOrderByCreatedAtDesc(user);
            } else {
                Set<Long> finalIds = matchedIds;
                bookmarks = bookmarkRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                    .filter(b -> finalIds.contains(b.getId()))
                    .collect(Collectors.toList());
            }
        } else if (title != null && !title.trim().isEmpty()) {
            bookmarks = bookmarkRepository.findAllByUserAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(user, title.trim());
        } else {
            bookmarks = bookmarkRepository.findAllByUserOrderByCreatedAtDesc(user);
        }

        // Apply sorting (by default sorted newest-first in DB, but let's support sort request if specified)
        if (sort != null && sort.contains("createdAt") && sort.contains("asc")) {
            bookmarks.sort(Comparator.comparing(Bookmark::getCreatedAt));
        } else {
            bookmarks.sort(Comparator.comparing(Bookmark::getCreatedAt).reversed());
        }

        List<BookmarkResponse> responses = bookmarks.stream()
            .map(BookmarkResponse::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(
            @RequestBody BookmarkRequest requestBody,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");

        if (requestBody.getUrl() == null || requestBody.getUrl().trim().isEmpty()) {
            throw new BadRequestException("URL is required");
        }
        if (requestBody.getTitle() == null || requestBody.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Title is required");
        }
        if (requestBody.getUrl().length() > 2048) {
            throw new BadRequestException("URL exceeds maximum length of 2048");
        }
        if (requestBody.getTitle().length() > 255) {
            throw new BadRequestException("Title exceeds maximum length of 255");
        }
        if (requestBody.getNotes() != null && requestBody.getNotes().length() > 10000) {
            throw new BadRequestException("Notes exceed maximum length of 10000");
        }

        Bookmark bookmark = new Bookmark(user, requestBody.getUrl().trim(), requestBody.getTitle().trim(), requestBody.getNotes());

        if (requestBody.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : requestBody.getTags()) {
                if (tagName == null || tagName.trim().isEmpty()) continue;
                String normalizedName = tagName.trim().toLowerCase();
                Tag tag = tagRepository.findByName(normalizedName)
                    .orElseGet(() -> tagRepository.save(new Tag(normalizedName)));
                tags.add(tag);
            }
            bookmark.setTags(tags);
        }

        Bookmark saved = bookmarkRepository.save(bookmark);
        return new ResponseEntity<>(new BookmarkResponse(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookmarkResponse> getBookmarkById(
            @PathVariable("id") Long id,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");

        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this bookmark");
        }

        return ResponseEntity.ok(new BookmarkResponse(bookmark));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookmarkResponse> updateBookmark(
            @PathVariable("id") Long id,
            @RequestBody BookmarkRequest requestBody,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");

        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this bookmark");
        }

        if (requestBody.getUrl() == null || requestBody.getUrl().trim().isEmpty()) {
            throw new BadRequestException("URL is required");
        }
        if (requestBody.getTitle() == null || requestBody.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Title is required");
        }
        if (requestBody.getUrl().length() > 2048) {
            throw new BadRequestException("URL exceeds maximum length of 2048");
        }
        if (requestBody.getTitle().length() > 255) {
            throw new BadRequestException("Title exceeds maximum length of 255");
        }
        if (requestBody.getNotes() != null && requestBody.getNotes().length() > 10000) {
            throw new BadRequestException("Notes exceed maximum length of 10000");
        }

        bookmark.setUrl(requestBody.getUrl().trim());
        bookmark.setTitle(requestBody.getTitle().trim());
        bookmark.setNotes(requestBody.getNotes());

        if (requestBody.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : requestBody.getTags()) {
                if (tagName == null || tagName.trim().isEmpty()) continue;
                String normalizedName = tagName.trim().toLowerCase();
                Tag tag = tagRepository.findByName(normalizedName)
                    .orElseGet(() -> tagRepository.save(new Tag(normalizedName)));
                tags.add(tag);
            }
            bookmark.setTags(tags);
        } else {
            bookmark.getTags().clear();
        }

        Bookmark updated = bookmarkRepository.save(bookmark);
        return ResponseEntity.ok(new BookmarkResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(
            @PathVariable("id") Long id,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("currentUser");

        Bookmark bookmark = bookmarkRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Bookmark not found"));

        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have access to this bookmark");
        }

        bookmarkRepository.delete(bookmark);
        return ResponseEntity.noContent().build();
    }
}
