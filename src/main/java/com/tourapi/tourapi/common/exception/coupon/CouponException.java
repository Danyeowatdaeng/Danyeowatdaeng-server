package com.tourapi.tourapi.common.exception.coupon;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.coupon.status.CouponErrorStatus;

public class CouponException extends GeneralException {
    
    public CouponException(CouponErrorStatus errorStatus) {
        super(errorStatus);
    }
    
    public CouponException(CouponErrorStatus errorStatus, String message) {
        super(errorStatus);
    }
}
