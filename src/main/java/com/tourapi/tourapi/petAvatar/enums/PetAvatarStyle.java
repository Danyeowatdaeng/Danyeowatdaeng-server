package com.tourapi.tourapi.petAvatar.enums;

public enum PetAvatarStyle {
    DEFAULT("기본"),
    ANIME("애니메이션"),
    CARTOON("카툰"),
    PIXEL("픽셀 아트"),
    WATERCOLOR("수채화"),
    DIGITAL_ART("디지털 아트");

    private final String displayName;

    PetAvatarStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
