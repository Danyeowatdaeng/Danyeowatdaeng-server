package com.tourapi.tourapi.mypet.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiaryListResponse {

    private List<DiaryResponse> diaries;
    private int totalCount;
    private boolean hasNext;
    private int currentPage;
    private int totalPages;

    public static DiaryListResponse from(List<DiaryResponse> diaries, int totalCount) {
        return DiaryListResponse.builder()
                .diaries(diaries != null ? diaries : List.of())
                .totalCount(totalCount)
                .hasNext(false)
                .currentPage(0)
                .totalPages(1)
                .build();
    }

    public static DiaryListResponse from(List<DiaryResponse> diaries, int totalCount,
                                         boolean hasNext, int currentPage, int totalPages) {
        return DiaryListResponse.builder()
                .diaries(diaries != null ? diaries : List.of())
                .totalCount(totalCount)
                .hasNext(hasNext)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .build();
    }
}