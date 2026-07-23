DROP INDEX IF EXISTS idx_bookmarks_user_title_lower;
ALTER TABLE bookmarks DROP COLUMN IF EXISTS title_lower;

DROP INDEX IF EXISTS idx_tags_name_lower;
ALTER TABLE tags DROP COLUMN IF EXISTS name_lower;
