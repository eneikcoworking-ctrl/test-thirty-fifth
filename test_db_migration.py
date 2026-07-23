import re

with open('src/test/java/com/bookmarks/BookmarkDatabaseTest.java', 'r') as f:
    content = f.read()

migration_test = """
    @Test
    public void testDatabaseMigrationDownScripts() throws Exception {
        // Read and run rollback for V3
        String downSqlPath3 = "db/migration/U3__add_user_constraints.sql";
        String downSql3 = new String(org.springframework.util.FileCopyUtils.copyToByteArray(new org.springframework.core.io.ClassPathResource(downSqlPath3).getInputStream()));
        for (String sql : downSql3.split(";")) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        // Read and run rollback for V2
        String downSqlPath2 = "db/migration/U2__add_user_email_and_updated_at.sql";
        String downSql2 = new String(org.springframework.util.FileCopyUtils.copyToByteArray(new org.springframework.core.io.ClassPathResource(downSqlPath2).getInputStream()));
        for (String sql : downSql2.split(";")) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        // Read and run rollback for V1
        String downSqlPath1 = "db/migration/U1__init_bookmarks_schema.sql";
        String downSql1 = new String(org.springframework.util.FileCopyUtils.copyToByteArray(new org.springframework.core.io.ClassPathResource(downSqlPath1).getInputStream()));
        for (String sql : downSql1.split(";")) {
            if (!sql.trim().isEmpty()) {
                jdbcTemplate.execute(sql.trim());
            }
        }

        // Verify that tables no longer exist
        java.util.List<String> tables = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'",
            String.class
        );
        assertThat(tables).doesNotContain("BOOKMARKS", "USERS", "TAGS", "BOOKMARK_TAGS");

        // Re-execute up migrations V1, V2, V3 to leave database in correct state
        String upSqlPath1 = "db/migration/V1__init_bookmarks_schema.sql";
        for (String sql : new String(org.springframework.util.FileCopyUtils.copyToByteArray(new org.springframework.core.io.ClassPathResource(upSqlPath1).getInputStream())).split(";")) {
            if (!sql.trim().isEmpty()) jdbcTemplate.execute(sql.trim());
        }

        String upSqlPath2 = "db/migration/V2__add_user_email_and_updated_at.sql";
        for (String sql : new String(org.springframework.util.FileCopyUtils.copyToByteArray(new org.springframework.core.io.ClassPathResource(upSqlPath2).getInputStream())).split(";")) {
            if (!sql.trim().isEmpty()) jdbcTemplate.execute(sql.trim());
        }

        String upSqlPath3 = "db/migration/V3__add_user_constraints.sql";
        for (String sql : new String(org.springframework.util.FileCopyUtils.copyToByteArray(new org.springframework.core.io.ClassPathResource(upSqlPath3).getInputStream())).split(";")) {
            if (!sql.trim().isEmpty()) jdbcTemplate.execute(sql.trim());
        }
    }
"""

if 'testDatabaseMigrationDownScripts' not in content:
    content = content.replace('}\n', test_migration + '\n}\n', 1)

with open('src/test/java/com/bookmarks/BookmarkDatabaseTest.java', 'w') as f:
    f.write(content)
