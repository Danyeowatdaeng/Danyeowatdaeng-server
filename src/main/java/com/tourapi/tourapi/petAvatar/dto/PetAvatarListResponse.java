package com.tourapi.tourapi.petAvatar.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PetAvatarListResponse {
    private List<PetAvatarResponse> petAvatars;
    private int totalCount;
    private boolean hasCustomAvatars;

    public static PetAvatarListResponse from(List<PetAvatarResponse> petAvatars) {
        boolean hasCustomAvatars = petAvatars.stream()
                .anyMatch(PetAvatarResponse::getIsCustom);
        
        return PetAvatarListResponse.builder()
                .petAvatars(petAvatars)
                .totalCount(petAvatars.size())
                .hasCustomAvatars(hasCustomAvatars)
                .build();
    }
}
