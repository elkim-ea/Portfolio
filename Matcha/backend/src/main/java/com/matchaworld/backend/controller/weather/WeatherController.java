package com.matchaworld.backend.controller.weather;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matchaworld.backend.weather.WeatherClient;
import com.matchaworld.backend.weather.WeatherInfo;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherClient weatherClient;

    public WeatherController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentWeather(
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon) {
        try {
            WeatherInfo info = weatherClient.getCurrentWeather(lat, lon);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("message", "날씨 조회 성공");
            body.put("data", info);

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "날씨 조회 실패: " + e.getMessage());
            error.put("data", null);

            return ResponseEntity.internalServerError().body(error);
        }
    }
}
