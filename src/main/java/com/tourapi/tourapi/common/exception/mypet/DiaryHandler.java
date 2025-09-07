package com.tourapi.tourapi.common.exception.mypet;

import com.tourapi.tourapi.common.exception.mypet.status.DiaryErrorStatus;
import com.tourapi.tourapi.common.exception.general.GeneralException;

public class DiaryHandler extends GeneralException {
    public DiaryHandler(DiaryErrorStatus status) {
        super(status);
    }
}