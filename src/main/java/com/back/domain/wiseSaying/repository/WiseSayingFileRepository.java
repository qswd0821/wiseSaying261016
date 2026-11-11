package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.util.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 파일 구현체.
//   {dir}/{번호}.json : 명언 하나
//   {dir}/lastId.txt  : 마지막으로 만든 번호 (삭제해도 줄지 않으므로 번호를 재사용하지 않는다)
//   {dir}/data.json   : 빌드 결과 (모든 명언 배열)
public class WiseSayingFileRepository implements WiseSayingRepository {
    private final Path dir;

    public WiseSayingFileRepository(String dirPath) {
        this.dir = Path.of(dirPath);
    }

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            int newId = getLastId() + 1;
            wiseSaying.setId(newId);
            setLastId(newId);
        }

        writeString(entityPath(wiseSaying.getId()), toJson(wiseSaying));

        return wiseSaying;
    }

    @Override
    public List<WiseSaying> findAll() {
        if (!Files.isDirectory(dir)) {
            return new ArrayList<>();
        }

        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                    .filter(path -> path.getFileName().toString().matches("\\d+\\.json"))
                    .map(path -> fromJson(readString(path)))
                    .sorted(Comparator.comparingInt(WiseSaying::getId))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        Path path = entityPath(id);

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        return Optional.of(fromJson(readString(path)));
    }

    @Override
    public boolean deleteById(int id) {
        try {
            return Files.deleteIfExists(entityPath(id));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void build() {
        String json = findAll().stream()
                .map(this::toJson)
                .map(objectJson -> objectJson.indent(2).stripTrailing())
                .collect(Collectors.joining(",\n", "[\n", "\n]"));

        if (json.equals("[\n\n]")) {
            json = "[]";
        }

        writeString(getBuildPath(), json);
    }

    public Path getBuildPath() {
        return dir.resolve("data.json");
    }

    public int getLastId() {
        Path path = lastIdPath();

        if (!Files.exists(path)) {
            return 0;
        }

        String text = readString(path).trim();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private void setLastId(int lastId) {
        writeString(lastIdPath(), String.valueOf(lastId));
    }

    private Path entityPath(int id) {
        return dir.resolve(id + ".json");
    }

    private Path lastIdPath() {
        return dir.resolve("lastId.txt");
    }

    private String toJson(WiseSaying wiseSaying) {
        return JsonUtil.toJson(Map.of(
                "id", wiseSaying.getId(),
                "content", wiseSaying.getContent(),
                "author", wiseSaying.getAuthor()
        ), List.of("id", "content", "author"));
    }

    private WiseSaying fromJson(String json) {
        Map<String, Object> map = JsonUtil.parseObject(json);

        return new WiseSaying(
                (Integer) map.get("id"),
                (String) map.get("content"),
                (String) map.get("author")
        );
    }

    private void writeString(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String readString(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
