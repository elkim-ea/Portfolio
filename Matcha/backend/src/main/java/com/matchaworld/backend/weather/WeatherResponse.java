package com.matchaworld.backend.weather;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResponse {

    private Current current;

    @Getter
    @Setter
    public static class Current {

        private double temperature_2m;
        private double relative_humidity_2m;
        private double pm10;
        private double pm2_5;
        private double uv_index;
    }
}
