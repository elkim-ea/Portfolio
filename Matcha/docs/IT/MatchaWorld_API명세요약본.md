# 🍵 Matcha World API 요약

## 1. Auth / 인증

**회원가입, 로그인, 이메일 인증, 비밀번호 재설정 등 사용자 인증 관련 기능**

| 기능               | Method | URL                                  | 설명                |
| ---------------- | ------ | ------------------------------------ | ----------------- |
| 회원가입 인증번호 전송     | POST   | `/api/auth/signup/send-code`         | 이메일 인증번호 발송       |
| 비밀번호 재설정 인증번호 전송 | POST   | `/api/auth/password-reset/send-code` | 비밀번호 재설정용 인증번호 발송 |
| 인증번호 확인          | POST   | `/api/auth/verify-code`              | 인증번호 유효성 확인       |
| 회원가입             | POST   | `/api/auth/signup`                   | 이메일 인증 후 계정 생성    |
| 로그인              | POST   | `/api/auth/login`                    | JWT 토큰 기반 로그인     |
| 비밀번호 재설정         | POST   | `/api/auth/password-reset`           | 이메일 인증 후 비밀번호 재설정 |
| 약관 전체 조회         | GET    | `/api/auth/terms`                    | 회원가입 약관 전체 조회     |
| 필수 약관 조회         | GET    | `/api/auth/terms/required`           | 필수 약관만 조회         |
| 약관 상세 조회         | GET    | `/api/auth/terms/{id}`               | 특정 약관 상세 조회       |

---

## 2. My / 마이페이지

**프로필 조회, 닉네임·비밀번호 변경, 대표 칭호 설정, 회원 탈퇴 등 사용자 개인 정보 관리**

| 기능       | Method | URL                               | 설명                      |
| -------- | ------ | --------------------------------- | ----------------------- |
| 내 프로필 조회 | GET    | `/api/my/profile`                 | 사용자 프로필 정보 조회           |
| 닉네임 변경   | PUT    | `/api/my/profile/nickname`        | 현재 비밀번호 확인 후 닉네임 변경     |
| 비밀번호 변경  | PUT    | `/api/my/profile/password`        | 현재 비밀번호 확인 후 새 비밀번호로 변경 |
| 대표 칭호 설정 | PUT    | `/api/my/profile/title/{titleId}` | 대표 칭호 지정                |
| 회원 탈퇴    | DELETE | `/api/my/profile`                 | 비밀번호 검증 후 계정 삭제         |

---

## 3. LifeLog / 기록하기

**사용자의 하루 기록 작성, 수정, 삭제, 조회 기능**

| 기능       | Method | URL                    | 설명                    |
| -------- | ------ | ---------------------- | --------------------- |
| 나의 기록 조회 | GET    | `/api/lifelog/me`      | 전체 또는 특정 날짜별 기록 조회    |
| 기록 추가    | POST   | `/api/lifelog`         | 새 기록 추가 (AI 분석 연동 포함) |
| 기록 수정    | PUT    | `/api/lifelog/{logId}` | 특정 기록 수정              |
| 기록 삭제    | DELETE | `/api/lifelog/{logId}` | 특정 기록 삭제              |

---

## 4. Quest / 퀘스트

**오늘의 퀘스트, 주간/시즌 퀘스트 조회 및 인증 제출 기능**

| 기능        | Method | URL                           | 설명                       |
| --------- | ------ | ----------------------------- | ------------------------ |
| 오늘의 퀘스트   | GET    | `/api/quest/today`            | 날씨 기반 자동 지급 퀘스트 조회       |
| 주간 퀘스트    | GET    | `/api/quest/weekly`           | 주간 퀘스트 목록 조회             |
| 시즌 퀘스트    | GET    | `/api/quest/season`           | 시즌 퀘스트 목록 조회             |
| 메인 통합 퀘스트 | GET    | `/api/quest/main`             | 오늘의 퀘스트 + 진행 중 퀘스트 통합 조회 |
| 퀘스트 인증 제출 | POST   | `/api/quest/{questId}/submit` | 이미지/텍스트 인증 제출 및 완료 처리    |

---

## 5. Company / 기업

**ESG 선도 및 후원 기업 목록 조회 기능**

| 기능           | Method | URL                    | 설명                    |
| ------------ | ------ | ---------------------- | --------------------- |
| ESG 선도 기업 조회 | GET    | `/api/company/leading` | LEADER 카테고리 기업 목록 조회  |
| ESG 후원 기업 조회 | GET    | `/api/company/sponsor` | SPONSOR 카테고리 기업 목록 조회 |

---

## 6. Activity / 활동

**사용자의 ESG 종합 점수, 통계, 그래프 데이터 제공**

| 기능          | Method | URL                | 설명                       |
| ----------- | ------ | ------------------ | ------------------------ |
| 내 ESG 종합 점수 | GET    | `/api/activity/me` | ESG 점수, 캐릭터 정보, 통합 점수 조회 |
| ESG 활동 통계   | GET    | `/api/activity`    | E(7일)/S(30일) 통계 및 로그 조회  |

---

## 7. Ranking / 랭킹

**ESG 점수 기반 글로벌 및 개인 랭킹 조회**

| 기능     | Method | URL                   | 설명                 |
| ------ | ------ | --------------------- | ------------------ |
| 글로벌 랭킹 | GET    | `/api/ranking/global` | ESG 점수 기반 상위 N명 랭킹 |
| 내 랭킹   | GET    | `/api/ranking/me`     | 개인 랭킹 조회           |

---

## 8. Admin / 관리자

**퀘스트, 칭호, 기업, 사용자 관리 기능 (관리자 전용)**

| 기능        | Method | URL                                    | 설명                      |
| --------- | ------ | -------------------------------------- | ----------------------- |
| 퀘스트 목록 조회 | GET    | `/api/admin/quests`                    | 퀘스트 목록 검색/조회            |
| 퀘스트 단건 조회 | GET    | `/api/admin/quests/{id}`               | 퀘스트 상세 조회               |
| 퀘스트 생성    | POST   | `/api/admin/quests`                    | 퀘스트 생성                  |
| 퀘스트 수정    | PUT    | `/api/admin/quests/{id}`               | 퀘스트 정보 수정               |
| 퀘스트 삭제    | DELETE | `/api/admin/quests/{id}`               | 퀘스트 삭제                  |
| 퀘스트 상태 토글 | PATCH  | `/api/admin/quests/{id}/toggle-active` | 활성/비활성 전환               |
| 칭호 목록 조회  | GET    | `/api/admin/titles`                    | 칭호 목록 조회                |
| 칭호 생성     | POST   | `/api/admin/titles`                    | 칭호 생성                   |
| 칭호 수정     | PUT    | `/api/admin/titles/{id}`               | 칭호 수정                   |
| 칭호 삭제     | DELETE | `/api/admin/titles/{id}`               | 칭호 삭제                   |
| 기업 목록 조회  | GET    | `/api/admin/companies`                 | 기업 목록 조회 (검색/페이지네이션 포함) |
| 기업 단건 조회  | GET    | `/api/admin/companies/{id}`            | 기업 상세 조회                |
| 기업 생성     | POST   | `/api/admin/companies`                 | 기업 등록 (파일 업로드 포함)       |
| 기업 수정     | PUT    | `/api/admin/companies/{id}`            | 기업 정보 수정 (파일 업로드 포함)    |
| 기업 삭제     | DELETE | `/api/admin/companies/{id}`            | 기업 삭제                   |
| 사용자 목록 조회 | GET    | `/api/admin/users`                     | 사용자 목록 조회 (검색/정렬)       |
| 사용자 단건 조회 | GET    | `/api/admin/users/{id}`                | 사용자 상세 조회               |
| 사용자 수정    | PUT    | `/api/admin/users/{id}`                | 사용자 정보 수정               |
| 사용자 삭제    | DELETE | `/api/admin/users/{id}`                | 사용자 삭제                  |
| 사용자 권한 변경 | PATCH  | `/api/admin/users/{id}/role`           | 권한(ROLE) 변경             |

---

## 9. AI / 인공지능

**이미지 기반 AI 분석 및 퀘스트 완료 연동 기능**

| 기능     | Method | URL                      | 설명                    |
| ------ | ------ | ------------------------ | --------------------- |
| 이미지 분석 | POST   | `/api/ai/image-analysis` | 이미지 분석 후 퀘스트 완료 자동 처리 |

---

## 10. Weather / 날씨

**실시간 날씨 조회 API (위도·경도 기반)**

| 기능       | Method | URL                                      | 설명               |
| -------- | ------ | ---------------------------------------- | ---------------- |
| 현재 날씨 조회 | GET    | `/api/weather/current?lat={위도}&lon={경도}` | 실시간 지역 날씨 데이터 조회 |

---

## 🔗 공통 특징

* 공통 Prefix: `/api`
* JWT 기반 인증 / CORS 허용: `localhost:5173`, `localhost:3000`
* 공통 응답 포맷: `{ success, message, data }`
* 날씨 API 결과를 활용해 “오늘의 퀘스트” 자동 지급 구조
