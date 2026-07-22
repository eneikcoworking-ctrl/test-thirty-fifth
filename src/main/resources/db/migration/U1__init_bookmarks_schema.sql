DROP INDEX IF EXISTS idx_bookmark_tags_tag_id;
DROP INDEX IF EXISTS idx_bookmarks_user_title;
DROP INDEX IF EXISTS idx_bookmarks_user_created_at;

DROP TABLE IF EXISTS bookmark_tags;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS users;
