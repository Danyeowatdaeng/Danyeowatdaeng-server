package com.tourapi.tourapi.member.dto;

import com.tourapi.tourapi.member.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private boolean signUpCompleted;
    private Long petAvatarId;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .signUpCompleted(member.isSignUpCompleted())
                .petAvatarId(member.getPetAvatar() != null ? member.getPetAvatar().getId() : null)
                .build();
    }
}
