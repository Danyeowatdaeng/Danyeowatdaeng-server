package com.tourapi.tourapi.petAvatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditRequest {

    @NotBlank
    @Size(max = 500)
    private String prompt;

    @NotBlank
    @Size(max = 2000)
    private String imageUrl;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}


