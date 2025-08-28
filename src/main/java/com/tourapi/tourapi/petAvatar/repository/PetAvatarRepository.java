package com.tourapi.tourapi.petAvatar.repository;

import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetAvatarRepository extends JpaRepository<PetAvatar, Long> {

    // 활성화된 PetAvatar 목록 조회
    List<PetAvatar> findByIsActiveTrue();

    // 펫 타입별 활성화된 PetAvatar 조회
    List<PetAvatar> findByPetAndIsActiveTrue(PetType pet);

    // 기본 PetAvatar만 조회 (커스텀 제외)
    List<PetAvatar> findByIsCustomFalseAndIsActiveTrue();

    // 펫 타입별 기본 PetAvatar 조회
    List<PetAvatar> findByPetAndIsCustomFalseAndIsActiveTrue(PetType pet);

    // 사용자별 커스텀 PetAvatar 조회
    List<PetAvatar> findByMemberIdAndIsCustomTrueAndIsActiveTrue(Long memberId);

    // 스타일별 PetAvatar 조회
    List<PetAvatar> findByStyleAndIsActiveTrue(PetAvatarStyle style);

    // 펫 타입과 스타일별 PetAvatar 조회
    List<PetAvatar> findByPetAndStyleAndIsActiveTrue(PetType pet, PetAvatarStyle style);

    // 코드로 PetAvatar 조회
    Optional<PetAvatar> findByCodeAndIsActiveTrue(String code);

    // 사용자별 모든 PetAvatar 조회 (기본 + 커스텀)
    @Query("SELECT pa FROM PetAvatar pa WHERE pa.isActive = true AND (pa.isCustom = false OR pa.memberId = :memberId)")
    List<PetAvatar> findAvailablePetAvatarsForMember(@Param("memberId") Long memberId);

    // 펫 타입별 사용자 사용 가능한 PetAvatar 조회
    @Query("SELECT pa FROM PetAvatar pa WHERE pa.pet = :pet AND pa.isActive = true AND (pa.isCustom = false OR pa.memberId = :memberId)")
    List<PetAvatar> findAvailablePetAvatarsForMemberByType(@Param("memberId") Long memberId, @Param("pet") PetType pet);

    // 커스텀 PetAvatar 개수 조회 (사용자별)
    long countByMemberIdAndIsCustomTrue(Long memberId);

    // 활성화된 PetAvatar 개수 조회 (펫 타입별)
    long countByPetAndIsActiveTrue(PetType pet);
}
