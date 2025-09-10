package com.tourapi.tourapi.common.exception.member.status;


import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum MemberErrorStatus implements ErrorResponse {

    // 회원
    @ExplainError("회원을 찾을 수 없음")
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "회원을 찾을 수 없습니다."),
    @ExplainError("이미 가입된 회원")
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER4002", "이미 가입된 회원입니다."),
    @ExplainError("약관 동의가 완료된 회원")
    MEMBER_ALREADY_SIGN_UP_COMPLETED(HttpStatus.BAD_REQUEST, "MEMBER4003", "약관 동의가 완료된 회원입니다."),
    @ExplainError("이미 탈퇴한 회원")
    ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "MEMBER4004", "이미 탈퇴한 회원입니다."),

    // 길티프리
    @ExplainError("길티프리는 주 1회만 활성화 가능")
    GUILTY_FREE_ACTIVATION_FORBIDDEN(HttpStatus.BAD_REQUEST, "MEMBER4010", "길티프리는 주 1회만 활성화할 수 있습니다."),
    @ExplainError("길티프리 조언 요청 불가")
    INVALID_ADVICE_REQUEST(HttpStatus.BAD_REQUEST, "MEMBER4011", "길티프리 조언을 요청할 수 없습니다."),
    @ExplainError("최근 길티프리 정보 없음")
    LAST_GUILTY_FREE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4012", "최근 길티프리 정보를 조회할 수 없습니다."),
    @ExplainError("길티프리 활성 내역 없음")
    GUILTY_FREE_NOT_ACTIVATED(HttpStatus.NOT_FOUND, "MEMBER4013", "길티프리 활성 내역이 없습니다."),

    @ExplainError("이미 다른 소셜 계정으로 가입된 이메일")
    DIFFERENT_SIGN_TYPE(HttpStatus.BAD_REQUEST, "MEMBER4020", "이미 다른 소셜 계정으로 가입된 이메일입니다."),

    //알림 설정
    @ExplainError("알림 설정이 존재하지 않음")
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4030", "알림 설정이 존재하지 않습니다."),
    @ExplainError("필수 약관 파일이 존재하지 않음")
    TERM_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4040", "필수 약관 파일이 존재하지 않습니다."),

    // OAuth 관련 에러
    @ExplainError("지원하지 않는 OAuth 공급자")
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.NOT_IMPLEMENTED, "MEMBER4050", "지원하지 않는 OAuth 공급자입니다."),
    @ExplainError("OAuth 설정이 올바르지 않음")
    OAUTH_CONFIG_INVALID(HttpStatus.SERVICE_UNAVAILABLE, "MEMBER4051", "OAuth 설정이 올바르지 않습니다."),
    @ExplainError("OAuth 콜백 처리 실패")
    OAUTH_CALLBACK_FAILED(HttpStatus.BAD_REQUEST, "MEMBER4052", "OAuth 콜백 처리에 실패했습니다."),
    @ExplainError("OAuth 토큰 교환 실패")
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_GATEWAY, "MEMBER4053", "OAuth 토큰 교환에 실패했습니다."),
    @ExplainError("OAuth 사용자 정보 조회 실패")
    OAUTH_USERINFO_FAILED(HttpStatus.BAD_GATEWAY, "MEMBER4054", "OAuth 사용자 정보 조회에 실패했습니다."),
    @ExplainError("OAuth state 검증 실패")
    OAUTH_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER4055", "OAuth state 검증에 실패했습니다."),
    @ExplainError("인증되지 않은 사용자")
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "MEMBER4056", "인증되지 않은 사용자입니다."),
    @ExplainError("OAuth 인증 코드 누락")
    OAUTH_CODE_MISSING(HttpStatus.BAD_REQUEST, "MEMBER4057", "OAuth 인증 코드가 누락되었습니다."),
    @ExplainError("OAuth 세션 만료")
    OAUTH_SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "MEMBER4058", "OAuth 세션이 만료되었습니다."),
    @ExplainError("OAuth 로그인 시작 실패")
    OAUTH_LOGIN_START_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MEMBER4059", "OAuth 로그인 시작에 실패했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MemberErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getErrorStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
