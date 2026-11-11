package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 메모리(리스트) 구현체. 프로그램을 끄면 데이터가 사라진다.
public class WiseSayingMemoryRepository implements WiseSayingRepository {
    private final List<WiseSaying> wiseSayings = new ArrayList<>();
    private int lastId = 0;

    @Override
    public WiseSaying save(WiseSaying wiseSaying) {
        if (wiseSaying.isNew()) {
            wiseSaying.setId(++lastId);
            wiseSayings.add(wiseSaying);
        }

        return wiseSaying;
    }

    @Override
    public List<WiseSaying> findAll() {
        return new ArrayList<>(wiseSayings);
    }

    @Override
    public Optional<WiseSaying> findById(int id) {
        return wiseSayings.stream()
                .filter(w -> w.getId() == id)
                .findFirst();
    }

    @Override
    public boolean deleteById(int id) {
        return wiseSayings.removeIf(w -> w.getId() == id);
    }

    @Override
    public void build() {
        throw new UnsupportedOperationException("메모리 리포지터리는 data.json 빌드를 지원하지 않습니다.");
    }
}
