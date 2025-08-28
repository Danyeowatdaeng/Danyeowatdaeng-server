package com.tourapi.tourapi.common.exception.terms;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.terms.status.TermsErrorStatus;

public class TermsHandler extends GeneralException {
    public TermsHandler(TermsErrorStatus status) {
        super(status);
    }
}
