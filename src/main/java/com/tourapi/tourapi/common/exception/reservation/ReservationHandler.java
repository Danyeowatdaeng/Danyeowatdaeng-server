package com.tourapi.tourapi.common.exception.reservation;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.reservation.status.ReservationErrorStatus;

public class ReservationHandler extends GeneralException {
    public ReservationHandler(ReservationErrorStatus status) {
        super(status);
    }
}

