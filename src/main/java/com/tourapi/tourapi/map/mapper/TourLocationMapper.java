package com.tourapi.tourapi.map.mapper;

import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.dto.ExternalTourLocationDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TourLocationMapper {

    public TourLocation toTourLocation(ExternalTourLocationDto externalDto) {
        if (externalDto == null) return null;
        return TourLocation.builder()
                .id(parseLong(externalDto.getContentid()))
                .title(externalDto.getTitle())
                .category(parseInt(externalDto.getContenttypeid()))
                .address(joinAddress(externalDto.getAddr1(), externalDto.getAddr2()))
                .imageUrl1(externalDto.getFirstimage())
                .imageUrl2(externalDto.getFirstimage2())
                .latitude(parseDouble(externalDto.getMapy()))
                .longitude(parseDouble(externalDto.getMapx()))
                .phoneNumber(externalDto.getTel())
                .homepageUrl(externalDto.getHomepage())
                .build();
    }

    public List<TourLocation> toTourLocationList(List<ExternalTourLocationDto> externalDtoList) {
        if (externalDtoList == null) return List.of();
        return externalDtoList.stream()
                .filter(Objects::nonNull)
                .map(this::toTourLocation)
                .collect(Collectors.toList());
    }

    private String joinAddress(String addr1, String addr2) {
        if (addr1 == null && addr2 == null) return null;
        if (addr1 != null && addr2 == null) return addr1;
        if (addr1 == null) return addr2;
        return addr1 + " " + addr2;
    }

    private Long parseLong(String v) { try { return v == null ? null : Long.parseLong(v); } catch (Exception e) { return null; } }
    private Integer parseInt(String v) { try { return v == null ? null : Integer.parseInt(v); } catch (Exception e) { return null; } }
    private Double parseDouble(String v) { try { return v == null ? null : Double.parseDouble(v); } catch (Exception e) { return null; } }
}


