CREATE TABLE users (
    id          UUID primary key,
    email       VARCHAR(512)  not null,
    password    VARCHAR(1024) not null,
    created_at  TIMESTAMP default now(),
    modified_at TIMESTAMP
);

CREATE UNIQUE INDEX users_email_uidx ON users(email);
