package com.tourapi.tourapi.common.exception.review;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.review.status.ReviewErrorStatus;

public class ReviewHandler extends GeneralException {
    public ReviewHandler(ReviewErrorStatus status) {
        super(status);
    }
}


