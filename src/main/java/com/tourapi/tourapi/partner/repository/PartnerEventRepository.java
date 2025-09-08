package com.tourapi.tourapi.partner.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tourapi.tourapi.partner.domain.PartnerEvent;

public interface PartnerEventRepository extends JpaRepository<PartnerEvent, Long> {
    List<PartnerEvent> findByPartnerPlace_IdAndStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            Long partnerPlaceId, String status, Instant now1, Instant now2);
}


