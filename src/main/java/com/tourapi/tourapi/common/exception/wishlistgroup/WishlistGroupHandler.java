package com.tourapi.tourapi.common.exception.wishlistgroup;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.wishlistgroup.status.WishlistGroupErrorStatus;

public class WishlistGroupHandler extends GeneralException {
    public WishlistGroupHandler(WishlistGroupErrorStatus status) {
        super(status);
    }
}
