ALTER TABLE tags ADD COLUMN name_lower VARCHAR(100) GENERATED ALWAYS AS (LOWER(name));
CREATE INDEX idx_tags_name_lower ON tags(name_lower);

ALTER TABLE bookmarks ADD COLUMN title_lower VARCHAR(255) GENERATED ALWAYS AS (LOWER(title));
CREATE INDEX idx_bookmarks_user_title_lower ON bookmarks(user_id, title_lower);
