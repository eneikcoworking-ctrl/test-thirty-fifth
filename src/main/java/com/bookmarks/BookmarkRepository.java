package com.bookmarks;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserOrderByCreatedAtDesc(User user);

    List<Bookmark> findAllByUserAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(User user, String title);

    List<Bookmark> findAllByUserAndTagsNameIgnoreCaseOrderByCreatedAtDesc(User user, String tagName);
}
