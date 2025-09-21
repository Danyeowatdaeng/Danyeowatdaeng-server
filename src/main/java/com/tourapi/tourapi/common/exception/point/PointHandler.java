package com.tourapi.tourapi.common.exception.point;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.point.status.PointErrorStatus;

public class PointHandler extends GeneralException {
    public PointHandler(PointErrorStatus status) {
        super(status);
    }
}