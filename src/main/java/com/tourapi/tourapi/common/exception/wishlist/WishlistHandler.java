package com.tourapi.tourapi.common.exception.wishlist;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistErrorStatus;

public class WishlistHandler extends GeneralException {
    public WishlistHandler(WishlistErrorStatus status) {
        super(status);
    }
}