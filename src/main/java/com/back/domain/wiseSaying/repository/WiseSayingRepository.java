package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId = 0;

    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    public int count() {
        return wiseSayings.size();
    }

    public List<WiseSaying> findAllDesc() {
        List<WiseSaying> result = new ArrayList<>(wiseSayings);
        Collections.reverse(result);
        return result;
    }

    // keyword 가 비어 있으면 전체, 아니면 keywordType(content/author)에 keyword 가 들어간 것만 최신순으로 골라
    // pageNo 페이지(1부터 시작)에 해당하는 최대 pageSize 개를 돌려준다.
    public Page<WiseSaying> findForListDesc(String keywordType, String keyword, int pageNo, int pageSize) {
        List<WiseSaying> filtered = findAllDesc().stream()
                .filter(w -> matches(w, keywordType, keyword))
                .collect(Collectors.toList());

        int fromIndex = Math.min((pageNo - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());

        return new Page<>(new ArrayList<>(filtered.subList(fromIndex, toIndex)), filtered.size(), pageNo, pageSize);
    }

    private boolean matches(WiseSaying wiseSaying, String keywordType, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }

        return switch (keywordType) {
            case "author" -> wiseSaying.getAuthor().contains(keyword);
            case "content" -> wiseSaying.getContent().contains(keyword);
            default -> wiseSaying.getContent().contains(keyword) || wiseSaying.getAuthor().contains(keyword);
        };
    }

    public Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(w -> w.getId() == id)
                .findFirst();
    }

    public boolean deleteById(int id) {
        return wiseSayings.removeIf(w -> w.getId() == id);
    }
}
