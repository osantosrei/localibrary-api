package com.localibrary.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class GoogleGeocodingResponse {
    private List<Result> results;
    private String status;

    @Data
    public static class Result {
        private Geometry geometry;
    }

    @Data
    public static class Geometry {
        private Location location;
    }

    @Data
    public static class Location {
        private Double lat;
        private Double lng;
    }
}