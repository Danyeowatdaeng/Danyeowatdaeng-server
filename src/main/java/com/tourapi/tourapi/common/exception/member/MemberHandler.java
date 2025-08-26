package com.tourapi.tourapi.common.exception.member;


import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;

public class MemberHandler extends GeneralException {
    public MemberHandler(MemberErrorStatus status) {
        super(status);
    }
}
