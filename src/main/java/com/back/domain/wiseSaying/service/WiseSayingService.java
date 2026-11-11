package com.back.domain.wiseSaying.service;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.standard.dto.Page;

import java.util.Optional;

public class WiseSayingService {
    private static final int PAGE_SIZE = 5;

    private final WiseSayingRepository wiseSayingRepository;

    public WiseSayingService(WiseSayingRepository wiseSayingRepository) {
        this.wiseSayingRepository = wiseSayingRepository;
    }

    public WiseSaying write(String content, String author) {
        return wiseSayingRepository.save(new WiseSaying(content, author));
    }

    // 명언이 하나도 없을 때만 "명언 1 / 작자미상 1" ~ "명언 n / 작자미상 n" 을 만든다.
    public void makeSampleDataIfEmpty(int count) {
        if (wiseSayingRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= count; i++) {
            write("명언 " + i, "작자미상 " + i);
        }
    }

    public Page<WiseSaying> findForList(String keywordType, String keyword, int pageNo) {
        return wiseSayingRepository.findForListDesc(keywordType, keyword, Math.max(1, pageNo), PAGE_SIZE);
    }

    public Optional<WiseSaying> findById(int id) {
        return wiseSayingRepository.findById(id);
    }

    public boolean delete(int id) {
        return wiseSayingRepository.deleteById(id);
    }

    public void build() {
        wiseSayingRepository.build();
    }

    public void modify(WiseSaying wiseSaying, String content, String author) {
        wiseSaying.setContent(content);
        wiseSaying.setAuthor(author);
        wiseSayingRepository.save(wiseSaying);
    }
}
