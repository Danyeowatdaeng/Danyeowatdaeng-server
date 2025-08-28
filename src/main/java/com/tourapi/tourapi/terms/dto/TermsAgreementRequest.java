package com.tourapi.tourapi.terms.dto;

import com.tourapi.tourapi.terms.TermsCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TermsAgreementRequest {
    
    @Schema(description = "동의할 약관 코드 목록", example = "[\"TERMS_OF_SERVICE\", \"PRIVACY_POLICY\"]")
    private List<TermsCode> termsCodes;
}
