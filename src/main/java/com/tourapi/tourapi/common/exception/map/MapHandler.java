package com.tourapi.tourapi.common.exception.map;


import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.map.status.MapErrorStatus;

public class MapHandler extends GeneralException {
    public MapHandler(MapErrorStatus status) {
        super(status);
    }
}


