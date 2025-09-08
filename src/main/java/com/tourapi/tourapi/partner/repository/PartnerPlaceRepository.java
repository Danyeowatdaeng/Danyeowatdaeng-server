package com.tourapi.tourapi.partner.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourapi.tourapi.partner.domain.PartnerPlace;

public interface PartnerPlaceRepository extends JpaRepository<PartnerPlace, Long> {
    Optional<PartnerPlace> findByContentIdAndPartnerId(Long contentId, String partnerId);
}


