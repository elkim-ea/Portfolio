import axios from "axios";

const uploadApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

// ✅ JWT 자동 첨부
uploadApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("token"); // 로그인 시 저장한 access token
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${token}`,
    };
  }
  return config;
});

export default uploadApi;
