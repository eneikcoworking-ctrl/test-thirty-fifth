package com.bookmarks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BookmarkDatabaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    public void testPersistAndRetrieveBookmarkWithTags() {
        // 1. Given: a valid user and tags
        User user = new User("john_doe", "hashed_password_123");
        user = userRepository.save(user);

        Tag tagTech = new Tag("tech");
        Tag tagJava = new Tag("java");
        tagTech = tagRepository.save(tagTech);
        tagJava = tagRepository.save(tagJava);

        // 2. When: saving a bookmark with url, title, notes and tags
        Bookmark bookmark = new Bookmark(user, "https://spring.io", "Spring Framework", "Awesome framework for Java");
        bookmark.getTags().add(tagTech);
        bookmark.getTags().add(tagJava);

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        assertThat(savedBookmark.getId()).isNotNull();

        // 3. Then: retrieve and assert
        Optional<Bookmark> retrievedOpt = bookmarkRepository.findById(savedBookmark.getId());
        assertThat(retrievedOpt).isPresent();
        Bookmark retrieved = retrievedOpt.get();

        assertThat(retrieved.getUrl()).isEqualTo("https://spring.io");
        assertThat(retrieved.getTitle()).isEqualTo("Spring Framework");
        assertThat(retrieved.getNotes()).isEqualTo("Awesome framework for Java");
        assertThat(retrieved.getUser().getUsername()).isEqualTo("john_doe");
        assertThat(retrieved.getTags())
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("tech", "java");
    }

    @Test
    @Transactional
    public void testFetchBookmarksSortedNewestFirst() {
        // 1. Given: a user and three bookmarks saved with different created_at timestamps
        User user = new User("jane_doe", "password_abc");
        user = userRepository.save(user);

        Instant now = Instant.now();

        Bookmark b1 = new Bookmark(user, "https://google.com", "Google", "Search Engine");
        b1.setCreatedAt(now.minus(5, ChronoUnit.MINUTES)); // 5 mins ago

        Bookmark b2 = new Bookmark(user, "https://github.com", "GitHub", "Code Hosting");
        b2.setCreatedAt(now); // newest

        Bookmark b3 = new Bookmark(user, "https://wikipedia.org", "Wikipedia", "Encyclopedia");
        b3.setCreatedAt(now.minus(10, ChronoUnit.MINUTES)); // oldest

        bookmarkRepository.save(b1);
        bookmarkRepository.save(b2);
        bookmarkRepository.save(b3);

        // 2. When: querying bookmarks
        List<Bookmark> bookmarks = bookmarkRepository.findAllByUserOrderByCreatedAtDesc(user);

        // 3. Then: verify sorted newest-first: b2 (now), b1 (5m ago), b3 (10m ago)
        assertThat(bookmarks).hasSize(3);
        assertThat(bookmarks.get(0).getTitle()).isEqualTo("GitHub");
        assertThat(bookmarks.get(1).getTitle()).isEqualTo("Google");
        assertThat(bookmarks.get(2).getTitle()).isEqualTo("Wikipedia");
    }

    @Test
    @Transactional
    public void testSearchByTitleAndFilterByTag() {
        // 1. Given: a user and categorized bookmarks
        User user = new User("alice", "pass");
        user = userRepository.save(user);

        Tag tech = tagRepository.save(new Tag("Technology"));
        Tag news = tagRepository.save(new Tag("News"));

        Bookmark b1 = new Bookmark(user, "https://techcrunch.com", "TechCrunch News", "Tech blog");
        b1.getTags().add(tech);
        b1.getTags().add(news);

        Bookmark b2 = new Bookmark(user, "https://bbc.com/news", "BBC News", "World news");
        b2.getTags().add(news);

        Bookmark b3 = new Bookmark(user, "https://developer.mozilla.org", "MDN Web Docs", "Web docs");
        b3.getTags().add(tech);

        bookmarkRepository.save(b1);
        bookmarkRepository.save(b2);
        bookmarkRepository.save(b3);

        // 2. When & Then: search by title "news" (case-insensitive)
        List<Bookmark> searchByTitle = bookmarkRepository.findAllByUserAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(user, "news");
        assertThat(searchByTitle).hasSize(2);
        assertThat(searchByTitle)
                .extracting(Bookmark::getTitle)
                .containsExactlyInAnyOrder("TechCrunch News", "BBC News");

        // 3. When & Then: filter by tag "technology" (case-insensitive)
        List<Bookmark> filterByTag = bookmarkRepository.findAllByUserAndTagsNameIgnoreCaseOrderByCreatedAtDesc(user, "technology");
        assertThat(filterByTag).hasSize(2);
        assertThat(filterByTag)
                .extracting(Bookmark::getTitle)
                .containsExactlyInAnyOrder("TechCrunch News", "MDN Web Docs");
    }

    @Test
    public void testDownMigrationScriptExecutesSuccessfully() throws IOException {
        // Read and run rollback for V3
        String downSqlPath3 = "src/main/resources/db/migration/U3__add_case_insensitive_indices.sql";
        String downSql3 = Files.readString(Paths.get(downSqlPath3));
        String[] sqlStatements3 = downSql3.split(";");
        for (String sql : sqlStatements3) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        // Read and run rollback for V2
        String downSqlPath2 = "src/main/resources/db/migration/U2__add_user_email_and_updated_at.sql";
        String downSql2 = Files.readString(Paths.get(downSqlPath2));
        String[] sqlStatements2 = downSql2.split(";");
        for (String sql : sqlStatements2) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        // Read and run rollback for V1
        String downSqlPath1 = "src/main/resources/db/migration/U1__init_bookmarks_schema.sql";
        String downSql1 = Files.readString(Paths.get(downSqlPath1));
        String[] sqlStatements1 = downSql1.split(";");
        for (String sql : sqlStatements1) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        // Verify that tables no longer exist
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'",
            String.class
        );
        assertThat(tables).doesNotContain("BOOKMARKS", "USERS", "TAGS", "BOOKMARK_TAGS");

        // Re-execute up migration V1, V2 and V3 to leave database in correct state
        String upSqlPath1 = "src/main/resources/db/migration/V1__init_bookmarks_schema.sql";
        String upSql1 = Files.readString(Paths.get(upSqlPath1));
        String[] upStatements1 = upSql1.split(";");
        for (String sql : upStatements1) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        String upSqlPath2 = "src/main/resources/db/migration/V2__add_user_email_and_updated_at.sql";
        String upSql2 = Files.readString(Paths.get(upSqlPath2));
        String[] upStatements2 = upSql2.split(";");
        for (String sql : upStatements2) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        String upSqlPath3 = "src/main/resources/db/migration/V3__add_case_insensitive_indices.sql";
        String upSql3 = Files.readString(Paths.get(upSqlPath3));
        String[] upStatements3 = upSql3.split(";");
        for (String sql : upStatements3) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }
    }
}
