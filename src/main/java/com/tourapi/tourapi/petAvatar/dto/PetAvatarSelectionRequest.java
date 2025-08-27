package com.tourapi.tourapi.petAvatar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PetAvatarSelectionRequest {
    
    @NotNull(message = "PetAvatar ID는 필수입니다")
    private Long petAvatarId;
}
