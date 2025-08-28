package com.tourapi.tourapi.common.exception.petAvatar;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;

public class PetAvatarHandler extends GeneralException {
    public PetAvatarHandler(PetAvatarErrorStatus status) {
        super(status);
    }
}
