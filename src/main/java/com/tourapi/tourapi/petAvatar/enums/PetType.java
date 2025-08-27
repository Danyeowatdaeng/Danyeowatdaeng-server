package com.tourapi.tourapi.petAvatar.enums;

public enum PetType {
    DOG("강아지"),
    CAT("고양이"),
    BIRD("새"),
    FISH("물고기"),
    RABBIT("토끼"),
    HAMSTER("햄스터"),
    TURTLE("거북이"),
    FERRET("페럿");

    private final String displayName;

    PetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
