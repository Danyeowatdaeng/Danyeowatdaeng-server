package com.tourapi.tourapi.common.exception.diary;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.diary.status.DiaryErrorStatus;

public class DiaryHandler extends GeneralException {
    public DiaryHandler(DiaryErrorStatus status) {
        super(status);
    }
}