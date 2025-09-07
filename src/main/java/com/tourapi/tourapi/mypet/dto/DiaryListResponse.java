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
    private int page;
    private int size;

    public static DiaryListResponse from(List<DiaryResponse> diaries, int totalCount, boolean hasNext, int page, int size) {
        return DiaryListResponse.builder()
                .diaries(diaries)
                .totalCount(totalCount)
                .hasNext(hasNext)
                .page(page)
                .size(size)
                .build();
    }
}