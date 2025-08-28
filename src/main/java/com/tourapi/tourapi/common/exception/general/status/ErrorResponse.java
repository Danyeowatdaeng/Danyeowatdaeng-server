package com.tourapi.tourapi.common.exception.general.status;

import org.springframework.http.HttpStatus;

public interface ErrorResponse {
    HttpStatus getErrorStatus();
    String getCode();
    String getMessage();
}
