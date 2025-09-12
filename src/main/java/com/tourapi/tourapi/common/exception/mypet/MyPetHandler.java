package com.tourapi.tourapi.common.exception.mypet;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.mypet.status.MyPetErrorStatus;

public class MyPetHandler extends GeneralException {
    public MyPetHandler(MyPetErrorStatus status) {
        super(status);
    }
}