package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.standard.dto.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 서비스는 이 인터페이스만 안다. 구현체(메모리/파일)를 바꿔 끼워도 서비스·컨트롤러는 그대로다.
public interface WiseSayingRepository {
    // 새 명언(id == 0)이면 번호를 붙여 저장하고, 기존 명언이면 덮어쓴다.
    WiseSaying save(WiseSaying wiseSaying);

    // 번호 오름차순
    List<WiseSaying> findAll();

    Optional<WiseSaying> findById(int id);

    boolean deleteById(int id);

    // 모든 명언을 data.json 으로 모은다.
    void build();

    default int count() {
        return findAll().size();
    }

    default List<WiseSaying> findAllDesc() {
        List<WiseSaying> result = new ArrayList<>(findAll());
        Collections.reverse(result);
        return result;
    }

    // keyword 가 비어 있으면 전체, 아니면 keywordType(content/author)에 keyword 가 들어간 것만 최신순으로 골라
    // pageNo 페이지(1부터 시작)에 해당하는 최대 pageSize 개를 돌려준다.
    default Page<WiseSaying> findForListDesc(String keywordType, String keyword, int pageNo, int pageSize) {
        List<WiseSaying> filtered = findAllDesc().stream()
                .filter(w -> matches(w, keywordType, keyword))
                .collect(Collectors.toList());

        int fromIndex = Math.min((pageNo - 1) * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());

        return new Page<>(new ArrayList<>(filtered.subList(fromIndex, toIndex)), filtered.size(), pageNo, pageSize);
    }

    private static boolean matches(WiseSaying wiseSaying, String keywordType, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }

        return switch (keywordType) {
            case "author" -> wiseSaying.getAuthor().contains(keyword);
            case "content" -> wiseSaying.getContent().contains(keyword);
            default -> wiseSaying.getContent().contains(keyword) || wiseSaying.getAuthor().contains(keyword);
        };
    }
}
