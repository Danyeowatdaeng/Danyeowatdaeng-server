package com.tourapi.tourapi.common.exception.token;


import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.token.status.TokenErrorStatus;

public class TokenHandler extends GeneralException {
    public TokenHandler(TokenErrorStatus status) {
        super(status);
    }
}
