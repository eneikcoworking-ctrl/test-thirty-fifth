ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_pwd_hash;
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_username_len;
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_email;
