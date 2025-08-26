package com.tourapi.tourapi.terms;

public enum TermsCode {
    TERMS_OF_SERVICE("이용약관"),
    PRIVACY_POLICY("개인정보 처리방침"),
    MARKETING_CONSENT("마케팅 정보 수신 동의"),
    LOCATION_SERVICE("위치 기반 서비스 이용약관");

    private final String description;

    TermsCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
