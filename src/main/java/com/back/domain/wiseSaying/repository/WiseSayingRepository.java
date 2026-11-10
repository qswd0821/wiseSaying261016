package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<WiseSaying> findAllDesc() {
        return wiseSayings.reversed();
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
