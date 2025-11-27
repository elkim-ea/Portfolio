import React, { useEffect, useState } from "react";
import api from "@/api/axios";
import type { CommonResponse } from "@/types/api";

type WeatherInfo = {
  temperature: number;
  humidity: number;
  pm10: number;
  pm25: number;
  uv: number;
};

export default function WeatherSection() {
  const [weather, setWeather] = useState<WeatherInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const WEATHER_PATH = "/weather/current";

  const fetchWeather = async (lat: number, lon: number) => {
    try {
      const res = await api.get<CommonResponse<WeatherInfo>>(WEATHER_PATH, {
        params: { lat, lon },
      });
      setWeather(res.data.data || null);
    } catch (e: any) {
      setError(e?.message || "날씨 불러오기 실패");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      (pos) => fetchWeather(pos.coords.latitude, pos.coords.longitude),
      () => setError("위치 권한이 거부되었습니다.")
    );
  }, []);

  return (
    <section className="bg-[#E8F5E9] rounded-lg shadow-lg p-6 text-center">
      <h3 className="text-lg font-bold text-gray-900 mb-2">오늘의 날씨</h3>
      {loading && <p className="text-sm text-gray-500">불러오는 중...</p>}
      {!loading && error && <p className="text-sm text-red-600">{error}</p>}
      {!loading && !error && weather && (
        <div>
          <p className="text-sm text-gray-700">
            기온 {weather.temperature.toFixed(1)}°C · 습도{" "}
            {Math.round(weather.humidity)}%
          </p>
          <p className="text-sm text-gray-700 mt-1">
            미세먼지 {Math.round(weather.pm10)} · PM2.5{" "}
            {Math.round(weather.pm25)} µg/m³
          </p>
          <p className="text-xs text-main-green mt-1">
            UV {weather.uv.toFixed(1)}
          </p>
        </div>
      )}
    </section>
  );
}
