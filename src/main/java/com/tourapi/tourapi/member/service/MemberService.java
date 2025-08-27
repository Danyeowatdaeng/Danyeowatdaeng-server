package com.tourapi.tourapi.member.service;

import com.tourapi.tourapi.member.Member;

public interface MemberService {
    
    /**
     * 인증된 사용자의 Member 정보를 조회합니다.
     * @param memberId 인증된 사용자의 ID
     * @return Member 엔티티
     * @throws GeneralException 회원을 찾을 수 없는 경우
     */
    Member getAuthenticatedMember(Long memberId);
    
    /**
     * 회원이 존재하는지 확인합니다.
     * @param memberId 회원 ID
     * @return 존재 여부
     */
    boolean existsById(Long memberId);
}
