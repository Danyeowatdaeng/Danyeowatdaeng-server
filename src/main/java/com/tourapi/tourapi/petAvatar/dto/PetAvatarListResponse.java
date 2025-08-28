package com.tourapi.tourapi.petAvatar.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Collections;

@Getter
@Builder
public class PetAvatarListResponse {
    private List<PetAvatarResponse> petAvatars;
    private int totalCount;
    private boolean hasCustomAvatars;

        public static PetAvatarListResponse from(List<PetAvatarResponse> petAvatars) {
               List<PetAvatarResponse> safe = (petAvatars != null) ? petAvatars : java.util.Collections.emptyList();
                boolean hasCustomAvatars = safe.stream()
                        .anyMatch(p -> Boolean.TRUE.equals(p.getIsCustom()));
        
                return PetAvatarListResponse.builder()
                        .petAvatars(safe)
                        .totalCount(safe.size())
                        .hasCustomAvatars(hasCustomAvatars)
                        .build();
            }
}
