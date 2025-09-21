package com.tourapi.tourapi.common.exception.walk;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.walk.status.WalkErrorStatus;

public class WalkHandler extends GeneralException {
    public WalkHandler(WalkErrorStatus status) {
        super(status);
    }
}