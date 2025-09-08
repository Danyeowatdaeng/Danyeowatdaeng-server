package com.tourapi.tourapi.partner.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tourapi.tourapi.partner.domain.PartnerEvent;
import com.tourapi.tourapi.partner.repository.PartnerEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartnerEventService {

    private final PartnerEventRepository eventRepository;

    public List<PartnerEvent> findActiveEvents(Long partnerPlaceId, Instant now) {
        return eventRepository.findByPartnerPlace_IdAndStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                partnerPlaceId, "ACTIVE", now, now);
    }
}


