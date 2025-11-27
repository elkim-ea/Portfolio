package com.matchaworld.backend.weather;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeatherInfo {

    private double temperature; // 기온
    private double humidity;    // 습도
    private double pm10;        // 미세먼지
    private double pm25;        // 초미세먼지
    private double uv;          // 자외선 지수
}
