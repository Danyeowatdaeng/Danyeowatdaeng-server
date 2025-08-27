package com.tourapi.tourapi.common.exception.petAvatar;

import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import lombok.Getter;

@Getter
public class PetAvatarHandler extends RuntimeException {
    
    private final PetAvatarErrorStatus errorStatus;
    
    public PetAvatarHandler(PetAvatarErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }
    
    public PetAvatarHandler(PetAvatarErrorStatus errorStatus, String message) {
        super(message);
        this.errorStatus = errorStatus;
    }
    
    public PetAvatarHandler(PetAvatarErrorStatus errorStatus, Throwable cause) {
        super(errorStatus.getMessage(), cause);
        this.errorStatus = errorStatus;
    }
}
