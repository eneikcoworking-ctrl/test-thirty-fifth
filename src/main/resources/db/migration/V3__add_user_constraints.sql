ALTER TABLE users ADD CONSTRAINT chk_users_email CHECK (email LIKE '%@%');
ALTER TABLE users ADD CONSTRAINT chk_users_username_len CHECK (LENGTH(username) >= 3);
ALTER TABLE users ADD CONSTRAINT chk_users_pwd_hash CHECK (LENGTH(password_hash) >= 60 AND password_hash LIKE '$2%');
