package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;
import com.back.standard.util.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// MySQL 구현체. JDBC 로 wise_saying 테이블에 저장한다.
// 번호는 AUTO_INCREMENT 가 매기므로, 삭제한 번호를 다시 쓰지 않고 재시작해도 이어진다.
public class WiseSayingDbRepository implements WiseSayingRepository {
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS wise_saying
            (
                id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                create_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                modify_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                content     TEXT         NOT NULL,
                author      VARCHAR(100) NOT NULL
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci
            """;

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final Path buildPath;

    public WiseSayingDbRepository(String jdbcUrl, String username, String password, String buildDirPath) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.buildPath = Path.of(buildDirPath, "data.json");

        createTableIfNotExists();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void createTableIfNotExists() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("DB 연결 또는 테이블 생성 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            insert(wiseSaying);
        } else {
            update(wiseSaying);
        }

        return wiseSaying;
    }

    private void insert(WiseSaying wiseSaying) {
        String sql = "INSERT INTO wise_saying (content, author) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, wiseSaying.getContent());
            ps.setString(2, wiseSaying.getAuthor());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                wiseSaying.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void update(WiseSaying wiseSaying) {
        String sql = "UPDATE wise_saying SET content = ?, author = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wiseSaying.getContent());
            ps.setString(2, wiseSaying.getAuthor());
            ps.setInt(3, wiseSaying.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<WiseSaying> findAll() {
        return query("SELECT id, content, author FROM wise_saying ORDER BY id ASC", List.of());
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        return query("SELECT id, content, author FROM wise_saying WHERE id = ?", List.of(id))
                .stream()
                .findFirst();
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM wise_saying WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int count() {
        return count("", List.of());
    }

    // 검색·페이징을 SQL 로 처리한다: WHERE ... LIKE, ORDER BY id DESC, LIMIT / OFFSET
    @Override
    public Page<WiseSaying> findForListDesc(String keywordType, String keyword, int pageNo, int pageSize) {
        String where = "";
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + escapeLike(keyword) + "%";

            switch (keywordType) {
                case "author" -> {
                    where = " WHERE author LIKE ?";
                    params.add(pattern);
                }
                case "content" -> {
                    where = " WHERE content LIKE ?";
                    params.add(pattern);
                }
                default -> {
                    where = " WHERE content LIKE ? OR author LIKE ?";
                    params.add(pattern);
                    params.add(pattern);
                }
            }
        }

        int totalCount = count(where, params);

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(pageSize);
        pageParams.add((pageNo - 1) * pageSize);

        List<WiseSaying> content = query(
                "SELECT id, content, author FROM wise_saying" + where + " ORDER BY id DESC LIMIT ? OFFSET ?",
                pageParams
        );

        return new Page<>(content, totalCount, pageNo, pageSize);
    }

    @Override
    public void build() {
        String json = findAll().stream()
                .map(w -> JsonUtil.toJson(
                        Map.of("id", w.getId(), "content", w.getContent(), "author", w.getAuthor()),
                        List.of("id", "content", "author")
                ))
                .map(objectJson -> objectJson.indent(2).stripTrailing())
                .collect(Collectors.joining(",\n", "[\n", "\n]"));

        if (json.equals("[\n\n]")) {
            json = "[]";
        }

        try {
            Files.createDirectories(buildPath.getParent());
            Files.writeString(buildPath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getBuildPath() {
        return buildPath;
    }

    // 테이블을 비우고 AUTO_INCREMENT 도 1로 되돌린다. (테스트 전용)
    public void truncate() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE wise_saying");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private int count(String where, List<Object> params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM wise_saying" + where)) {
            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<WiseSaying> query(String sql, List<Object> params) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                List<WiseSaying> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(new WiseSaying(rs.getInt("id"), rs.getString("content"), rs.getString("author")));
                }

                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    // LIKE 의 특수문자(%, _, \)를 글자 그대로 찾도록 이스케이프한다.
    private static String escapeLike(String keyword) {
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
