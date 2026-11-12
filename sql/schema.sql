-- 명언 앱 MySQL 스키마
-- 개발 DB(wise_saying_dev)와 테스트 DB(wise_saying_test)를 따로 만든다.

CREATE DATABASE IF NOT EXISTS wise_saying_dev
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS wise_saying_test
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wise_saying_dev.wise_saying
(
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    create_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modify_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    content     TEXT         NOT NULL,
    author      VARCHAR(100) NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wise_saying_test.wise_saying LIKE wise_saying_dev.wise_saying;
