# IT·기술 영상 만들기

## 유튜브 채널

- 채널 URL: https://youtube.com/channel/UCm-N9RO-IwS7MSqh_InBqwA?si=cEOxNHmzrtEyUgWZ

## 제출 영상

- 주제: MySQL과 SQL의 차이
- 영상 URL: https://youtube.com/shorts/5gv-FN4AC7o?si=Mdtx9Qa07k8XkWLA

---

# 명언 앱 (MySQL)

콘솔 명언 앱입니다. 명언을 MySQL 에 저장합니다.

## 1. MySQL 띄우기

Docker 를 쓰는 경우 (포트 3307):

```bash
docker run -d --name wise-saying-mysql -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=<비밀번호> -e TZ=Asia/Seoul \
  mysql:latest --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

로컬에 설치한 MySQL 을 써도 됩니다. 포트만 아래 접속 정보에 맞춰 주세요.

## 2. DB·테이블 만들기

[`sql/schema.sql`](sql/schema.sql) 이 개발 DB(`wise_saying_dev`)와 테스트 DB(`wise_saying_test`), 그리고 각 DB 의 `wise_saying` 테이블을 만듭니다.

```bash
docker exec -i wise-saying-mysql mysql -uroot -p<비밀번호> < sql/schema.sql
```

| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | `INT UNSIGNED AUTO_INCREMENT PK` | 명언 번호. 삭제해도 재사용하지 않음 |
| `create_date` | `DATETIME` | 등록 시각 |
| `modify_date` | `DATETIME` | 수정 시각 (`ON UPDATE CURRENT_TIMESTAMP`) |
| `content` | `TEXT` | 명언 |
| `author` | `VARCHAR(100)` | 작가 |

앱이 시작할 때도 `CREATE TABLE IF NOT EXISTS` 로 테이블을 만들기 때문에, DB 만 있으면 됩니다.

## 3. 접속 정보

비밀번호는 소스에 넣지 않습니다. 아래 순서로 읽습니다.

1. 환경변수 `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
2. `config/db.properties` (git 에 올리지 않음)

```bash
cp config/db.properties.example config/db.properties
# db.password 등을 채운다
```

DB 이름은 실행 모드로 정해집니다. 앱 실행은 `wise_saying_dev`, 테스트는 `wise_saying_test` 를 씁니다.

## 4. 실행과 테스트

```bash
./gradlew run --console=plain -q
./gradlew test
```

테스트는 매 테스트 전에 `wise_saying_test.wise_saying` 을 `TRUNCATE` 해서 번호를 1부터 다시 시작합니다.

## 리포지터리를 어떻게 갈아 끼웠나

`WiseSayingService` 는 `WiseSayingRepository` 인터페이스만 압니다. 구현체는 세 가지이고, 어떤 것을 쓸지는 `AppContext.init()` 한 곳에서만 정합니다.

| 구현체 | 저장 위치 |
|---|---|
| `WiseSayingMemoryRepository` | 메모리 리스트 |
| `WiseSayingFileRepository` | `db/wiseSaying/{번호}.json`, `lastId.txt` |
| `WiseSayingDbRepository` (현재 사용) | MySQL `wise_saying` 테이블 (JDBC) |

MySQL 로 바꿀 때 컨트롤러·서비스는 고치지 않았습니다. `WiseSayingDbRepository` 는 검색·페이징을 SQL(`WHERE ... LIKE`, `ORDER BY id DESC`, `LIMIT ? OFFSET ?`)로 처리하고, `빌드` 명령은 DB 의 모든 명언을 `db/wiseSaying/data.json` 으로 저장합니다.
