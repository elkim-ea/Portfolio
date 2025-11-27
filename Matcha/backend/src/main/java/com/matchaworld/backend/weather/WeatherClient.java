package com.matchaworld.backend.weather;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WeatherClient {

    // 외부 API 기본 URL
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String AIR_URL = "https://air-quality-api.open-meteo.com/v1/air-quality";

    // RestTemplate (HTTP 요청용)
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 현재 날씨와 공기질 데이터를 조회하는 메서드. 외부 API 호출 실패 시 기본값을 반환.
     */
    public WeatherInfo getCurrentWeather(double latitude, double longitude) {
        try {
            // 날씨 API URL 생성
            String weatherUrl = UriComponentsBuilder.fromHttpUrl(WEATHER_URL)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "temperature_2m,relative_humidity_2m")
                    .queryParam("hourly", "uv_index") // 시간별 자외선 데이터
                    .toUriString();

            // 날씨 API 호출
            JsonNode weatherRes = restTemplate.getForObject(weatherUrl, JsonNode.class);

            // 응답 유효성 검증
            if (weatherRes == null || weatherRes.path("current").isMissingNode()) {
                throw new IllegalStateException("Weather API 응답이 비정상입니다.");
            }

            // 기온/습도 추출
            double temperature = weatherRes.path("current").path("temperature_2m").asDouble(25.0);
            double humidity = weatherRes.path("current").path("relative_humidity_2m").asDouble(60.0);

            // 자외선(UV) 데이터 추출
            JsonNode uvArray = weatherRes.path("hourly").path("uv_index");
            double uv = 0.0;
            if (uvArray.isArray() && uvArray.size() > 0) {
                uv = uvArray.get(uvArray.size() - 1).asDouble(0.0);
            }

            // 공기질 API URL 생성
            String airUrl = UriComponentsBuilder.fromHttpUrl(AIR_URL)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "pm10,pm2_5")
                    .toUriString();

            // 공기질 API 호출
            JsonNode airRes = restTemplate.getForObject(airUrl, JsonNode.class);

            // ✅ 응답 유효성 검증
            if (airRes == null || airRes.path("current").isMissingNode()) {
                throw new IllegalStateException("Air Quality API 응답이 비정상입니다.");
            }

            // 미세먼지(PM10) / 초미세먼지(PM2.5)
            double pm10 = airRes.path("current").path("pm10").asDouble(20.0);
            double pm25 = airRes.path("current").path("pm2_5").asDouble(10.0);

            // 성공 시 WeatherInfo 반환
            return new WeatherInfo(temperature, humidity, pm10, pm25, uv);

        } catch (Exception e) {
            // 모든 예외를 처리하고 기본값으로 fallback
            log.error("🌩️ WeatherClient 오류 발생: {}", e.getMessage(), e);
            return new WeatherInfo(25.0, 60.0, 20.0, 10.0, 0.0);
        }
    }
}
