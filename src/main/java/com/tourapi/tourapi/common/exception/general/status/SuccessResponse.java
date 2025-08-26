package com.tourapi.tourapi.common.exception.general.status;

import org.springframework.http.HttpStatus;

public interface SuccessResponse {
    HttpStatus getSuccessStatus();
    String getCode();
    String getMessage();
}
