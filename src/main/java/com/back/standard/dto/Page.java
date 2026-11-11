package com.back.standard.dto;

import java.util.List;

// 한 페이지 분량의 결과와 페이징 정보를 함께 담는다.
public class Page<T> {
    private final List<T> content;
    private final int totalCount;
    private final int pageNo;
    private final int pageSize;

    public Page(List<T> content, int totalCount, int pageNo, int pageSize) {
        this.content = content;
        this.totalCount = totalCount;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public List<T> getContent() {
        return content;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    // 결과가 0개여도 1페이지는 있다고 본다.
    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
    }
}
