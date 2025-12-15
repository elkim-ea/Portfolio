-- =================================================================
--  테이블 생성 (Schema Definition)
-- =================================================================

-- 테이블이 존재하면 삭제 (개발 초기 단계에서 스키마 변경 시 유용)
DROP TABLE IF EXISTS USER_TERMS, TERMS, AUTH_CODE, COMPANY_CATEGORY, ESG_CATEGORY, COMPANY, USER_TITLE, TITLE, CERTIFICATION, USER_QUEST, QUEST, LIFE_LOG, USER;

-- 사용자 테이블 (USER)
CREATE TABLE USER (
    USER_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    EMAIL VARCHAR(100) NOT NULL UNIQUE,
    PASSWORD VARCHAR(255) NOT NULL,
    NICKNAME VARCHAR(50) NOT NULL UNIQUE,
    `CHARACTER` TEXT NOT NULL,
    ESG_SCORE INT NOT NULL DEFAULT 0,
    E_SCORE INT NOT NULL DEFAULT 0,
    S_SCORE INT NOT NULL DEFAULT 0,
    `ROLE` ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    CREATED_AT DATETIME NOT NULL,
    UPDATED_AT DATETIME NOT NULL
);

-- 라이프 로그 테이블 (LIFE_LOG)
CREATE TABLE LIFE_LOG (
    LOG_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    USER_ID BIGINT NOT NULL,
    CONTENT VARCHAR(255) NOT NULL,
    CATEGORY ENUM('E', 'S') NOT NULL,
    LOGGED_AT DATETIME NOT NULL,
    ESG_SCORE_EFFECT DECIMAL(3,2) NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE
);

-- 퀘스트 테이블 (QUEST)
CREATE TABLE QUEST (
    QUEST_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    ADMIN_ID BIGINT NULL,
    TITLE VARCHAR(100) NOT NULL,
    DESCRIPTION TEXT,
    REWARD_SCORE INT NOT NULL,
    CATEGORY ENUM('E', 'S') NOT NULL DEFAULT 'E', 
    `TYPE` ENUM('DAILY', 'WEEKLY', 'SEASON') NOT NULL,
    AUTH_TYPE ENUM('IMAGE', 'TEXT') NOT NULL,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
    MAX_ATTEMPTS INT NOT NULL DEFAULT 1,
    CONDITION_JSON JSON,
    CREATED_AT DATETIME NOT NULL,
    FOREIGN KEY (ADMIN_ID) REFERENCES USER(USER_ID) ON DELETE SET NULL
);

-- 사용자 퀘스트 수행 테이블 (USER_QUEST)
CREATE TABLE USER_QUEST (
    UQ_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    USER_ID BIGINT NOT NULL,
    QUEST_ID BIGINT NOT NULL,
    STATUS ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING',
    ATTEMPT_COUNT INT NOT NULL DEFAULT 0,
    STARTED_AT DATETIME,
    COMPLETED_AT DATETIME,
    FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (QUEST_ID) REFERENCES QUEST(QUEST_ID) ON DELETE CASCADE,
    UNIQUE (USER_ID, QUEST_ID)
);

-- 인증 및 보상 테이블 (CERTIFICATION)
CREATE TABLE CERTIFICATION (
    CERT_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    UQ_ID BIGINT NOT NULL,
    USER_ID BIGINT NOT NULL,
    AUTH_TYPE ENUM('IMAGE', 'TEXT') NOT NULL,
    AUTH_CONTENT TEXT NOT NULL,
    VALIDATION_STATUS ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING',
    VALIDATED_AT DATETIME,
    CREATED_AT DATETIME NOT NULL,
    MODEL_TYPE ENUM('OPENAPI', 'HUGGING') NOT NULL,
    CONFIDENCE_SCORE DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (UQ_ID) REFERENCES USER_QUEST(UQ_ID) ON DELETE CASCADE,
    FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE
);

-- 칭호 테이블 (TITLE)
CREATE TABLE TITLE (
    TITLE_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    NAME VARCHAR(50) NOT NULL UNIQUE,
    DESCRIPTION VARCHAR(255) NOT NULL,
    CONDITION_JSON JSON NOT NULL
);

-- 사용자 획득 칭호 테이블 (USER_TITLE)
CREATE TABLE USER_TITLE (
    UT_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    USER_ID BIGINT NOT NULL,
    TITLE_ID BIGINT NOT NULL,
    EARNED_AT DATETIME NOT NULL,
    IS_MAIN BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (TITLE_ID) REFERENCES TITLE(TITLE_ID) ON DELETE CASCADE,
    UNIQUE (USER_ID, TITLE_ID)
);

-- ESG 기업 테이블 (COMPANY)
CREATE TABLE COMPANY (
    COMPANY_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    COMPANY_NAME VARCHAR(100) NOT NULL UNIQUE,
    COMPANY_LOGO TEXT NOT NULL,
    COMPANY_WEBSITE_URL TEXT NOT NULL,
    CREATED_AT DATETIME NOT NULL,
    UPDATED_AT DATETIME DEFAULT NULL 
);

-- ESG 활동 카테고리 테이블 (ESG_CATEGORY)
CREATE TABLE ESG_CATEGORY (
    CATEGORY_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    CATEGORY_NAME VARCHAR(100) NOT NULL
);

-- ESG 활동 카테고리별 기업 테이블 (COMPANY_CATEGORY)
CREATE TABLE COMPANY_CATEGORY (
    COMPANY_ID BIGINT NOT NULL,
    CATEGORY_ID BIGINT NOT NULL,
    PRIMARY KEY (COMPANY_ID, CATEGORY_ID),
    FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(COMPANY_ID) ON DELETE CASCADE,
    FOREIGN KEY (CATEGORY_ID) REFERENCES ESG_CATEGORY(CATEGORY_ID) ON DELETE CASCADE
);

-- 개인정보 인증 테이블 (AUTH_CODE)
CREATE TABLE AUTH_CODE (
    AUTH_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    EMAIL VARCHAR(100) NOT NULL,
    AUTH_CODE VARCHAR(6) NOT NULL,
    EXPIRY_TIME DATETIME NOT NULL,
    IS_VERIFIED BOOLEAN NOT NULL DEFAULT FALSE,
    CREATED_AT DATETIME NOT NULL
);

-- 약관 정의 테이블 (TERMS)
CREATE TABLE TERMS (
    TERM_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    TITLE VARCHAR(100) NOT NULL,
    VERSION VARCHAR(20) NOT NULL,
    CONTENT TEXT NOT NULL,
    IS_REQUIRED BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_AT DATETIME NOT NULL
);

-- 사용자 약관 동의 기록 테이블 (USER_TERMS)
CREATE TABLE USER_TERMS (
    UT_ID BIGINT PRIMARY KEY AUTO_INCREMENT,
    USER_ID BIGINT NOT NULL,
    TERM_ID BIGINT NOT NULL,
    IS_AGREED BOOLEAN NOT NULL,
    AGREED_AT DATETIME NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (TERM_ID) REFERENCES TERMS(TERM_ID) ON DELETE CASCADE,
    UNIQUE (USER_ID, TERM_ID)
);

-- =======================================
-- ✅ 추가 스키마 수정 (2025-10-31)
-- =======================================

-- QUEST 테이블 CATEGORY 컬럼 추가
ALTER TABLE QUEST
ADD COLUMN IF NOT EXISTS CATEGORY ENUM('E', 'S') NOT NULL DEFAULT 'E';

-- USER_TITLE 테이블 IS_MAIN 컬럼 추가
ALTER TABLE USER_TITLE
ADD COLUMN IF NOT EXISTS IS_MAIN BOOLEAN NOT NULL DEFAULT FALSE;

-- USER_TITLE 테이블 인덱스 추가 (대표 칭호 제한용)
CREATE INDEX IF NOT EXISTS idx_user_title_is_main
ON USER_TITLE(USER_ID, IS_MAIN);

-- =================================================================
--  더미 데이터 삽입 (Dummy Data Insertion)
-- =================================================================
-- USE matcha_world_dev_db; -- 개발용 DB
USE matcha_world_prod_db; -- ncp 운영용 DB, gcp 운영용 DB 동일
-- 사용자 (USER) 비밀번호 모두 @1234567 입니당~
SET @EXAMPLE_HASH = '$2a$10$EHblQZeH/Re6Rop7fwpGXOEUjfM/MXbJicHLmCDsw5O1kDWxDP4j.';
INSERT INTO USER (EMAIL, PASSWORD, NICKNAME, `CHARACTER`, ESG_SCORE, E_SCORE, S_SCORE, `ROLE`, CREATED_AT, UPDATED_AT) VALUES
('matcha@gmail.com', @EXAMPLE_HASH, '마차씌', '/uploads/character/flower.png', 150, 80, 70, 'ADMIN', '2025-04-28 10:22:00', '2025-04-28 09:10:00'),
('greenbean@example.com', @EXAMPLE_HASH, '그린콩', '/uploads/character/tree.png', 480, 300, 180, 'USER', '2025-05-01 10:22:00', '2025-05-15 09:10:00'),
('earth@example.com', @EXAMPLE_HASH, '지구랑', '/uploads/character/earth.png', 650, 390, 260, 'USER', '2025-05-04 14:30:00', '2025-05-19 16:05:00'),
('user04@example.com', @EXAMPLE_HASH, '플로라', '/uploads/character/flower.png', 200, 120, 80, 'USER', '2025-06-02 11:20:00', '2025-06-20 08:50:00'),
('user05@example.com', @EXAMPLE_HASH, '문라잇', '/uploads/character/moon.png', 950, 570, 380, 'USER', '2025-06-10 09:40:00', '2025-06-28 13:10:00'),
('user06@example.com', @EXAMPLE_HASH, '씨앗요정', '/uploads/character/seed.png', 100, 60, 40, 'USER', '2025-07-02 10:00:00', '2025-07-19 18:15:00'),
('user07@example.com', @EXAMPLE_HASH, '별빛지킴이', '/uploads/character/star.png', 1300, 780, 520, 'USER', '2025-07-18 14:25:00', '2025-07-29 19:45:00'),
('user08@example.com', @EXAMPLE_HASH, '햇살이', '/uploads/character/sun.png', 1700, 1020, 680, 'USER', '2025-07-22 11:10:00', '2025-08-03 09:50:00'),
('user09@example.com', @EXAMPLE_HASH, '트리플랜', '/uploads/character/tree.png', 400, 240, 160, 'USER', '2025-08-01 13:40:00', '2025-08-15 08:00:00'),
('user10@example.com', @EXAMPLE_HASH, '바람결', '/uploads/character/wind.png', 2200, 1320, 880, 'USER', '2025-08-07 16:20:00', '2025-08-25 11:35:00'),
('user11@example.com', @EXAMPLE_HASH, '에코냥', '/uploads/character/cloud.png', 2600, 1560, 1040, 'USER', '2025-08-18 09:10:00', '2025-09-02 17:00:00'),
('user12@example.com', @EXAMPLE_HASH, '지구별이', '/uploads/character/earth.png', 650, 390, 260, 'USER', '2025-09-03 10:15:00', '2025-09-16 09:30:00'),
('user13@example.com', @EXAMPLE_HASH, '플랜비', '/uploads/character/flower.png', 200, 120, 80, 'USER', '2025-09-08 08:45:00', '2025-09-24 15:20:00'),
('user14@example.com', @EXAMPLE_HASH, '문도리', '/uploads/character/moon.png', 950, 570, 380, 'USER', '2025-09-15 11:00:00', '2025-09-29 13:50:00'),
('user15@example.com', @EXAMPLE_HASH, '씨앗링', '/uploads/character/seed.png', 100, 60, 40, 'USER', '2025-09-22 10:40:00', '2025-10-03 14:00:00'),
('user16@example.com', @EXAMPLE_HASH, '별콩', '/uploads/character/star.png', 1300, 780, 520, 'USER', '2025-09-28 12:10:00', '2025-10-08 11:45:00'),
('user17@example.com', @EXAMPLE_HASH, '썬샤인', '/uploads/character/sun.png', 1700, 1020, 680, 'USER', '2025-10-01 09:10:00', '2025-10-15 16:20:00'),
('user18@example.com', @EXAMPLE_HASH, '숲속이', '/uploads/character/tree.png', 400, 240, 160, 'USER', '2025-10-03 13:30:00', '2025-10-20 08:40:00'),
('user19@example.com', @EXAMPLE_HASH, '윈디', '/uploads/character/wind.png', 2200, 1320, 880, 'USER', '2025-10-05 09:00:00', '2025-10-21 17:30:00'),
('user20@example.com', @EXAMPLE_HASH, '리사이클링', '/uploads/character/cloud.png', 2600, 1560, 1040, 'USER', '2025-10-10 08:30:00', '2025-10-25 10:50:00'),
('user21@example.com', @EXAMPLE_HASH, '에버블루', '/uploads/character/earth.png', 650, 390, 260, 'USER', '2025-10-14 11:10:00', '2025-10-30 12:25:00'),
('user22@example.com', @EXAMPLE_HASH, '플랜그린', '/uploads/character/flower.png', 200, 120, 80, 'USER', '2025-10-18 15:20:00', '2025-11-02 09:10:00'),
('user23@example.com', @EXAMPLE_HASH, '문버디', '/uploads/character/moon.png', 950, 570, 380, 'USER', '2025-10-22 08:00:00', '2025-11-04 14:10:00'),
('user24@example.com', @EXAMPLE_HASH, '씨드봉봉', '/uploads/character/seed.png', 100, 60, 40, 'USER', '2025-10-25 09:50:00', '2025-11-06 11:00:00'),
('user25@example.com', @EXAMPLE_HASH, '별빛냥', '/uploads/character/star.png', 1300, 780, 520, 'USER', '2025-10-27 13:15:00', '2025-11-08 18:40:00'),
('user26@example.com', @EXAMPLE_HASH, '햇살콩', '/uploads/character/sun.png', 1700, 1020, 680, 'USER', '2025-10-30 09:10:00', '2025-11-09 10:10:00'),
('user27@example.com', @EXAMPLE_HASH, '트리팡', '/uploads/character/tree.png', 400, 240, 160, 'USER', '2025-11-01 08:30:00', '2025-11-10 11:50:00'),
('user28@example.com', @EXAMPLE_HASH, '바람순이', '/uploads/character/wind.png', 2200, 1320, 880, 'USER', '2025-11-02 13:10:00', '2025-11-11 17:40:00'),
('user29@example.com', @EXAMPLE_HASH, '에코루', '/uploads/character/cloud.png', 2600, 1560, 1040, 'USER', '2025-11-03 14:00:00', '2025-11-12 19:00:00'),
('user30@example.com', @EXAMPLE_HASH, '지구쥬', '/uploads/character/earth.png', 650, 390, 260, 'USER', '2025-11-04 10:30:00', '2025-11-13 09:40:00'),
('user31@example.com', @EXAMPLE_HASH, '플로윙', '/uploads/character/flower.png', 200, 120, 80, 'USER', '2025-11-05 09:20:00', '2025-11-14 12:20:00'),
('user32@example.com', @EXAMPLE_HASH, '문콩', '/uploads/character/moon.png', 950, 570, 380, 'USER', '2025-11-06 08:10:00', '2025-11-15 15:30:00'),
('user33@example.com', @EXAMPLE_HASH, '새싹별', '/uploads/character/seed.png', 100, 60, 40, 'USER', '2025-11-07 10:45:00', '2025-11-16 18:10:00'),
('user34@example.com', @EXAMPLE_HASH, '별냥이', '/uploads/character/star.png', 1300, 780, 520, 'USER', '2025-11-08 11:30:00', '2025-11-17 09:00:00'),
('user35@example.com', @EXAMPLE_HASH, '태양봄', '/uploads/character/sun.png', 1700, 1020, 680, 'USER', '2025-11-09 09:50:00', '2025-11-18 08:10:00');

-- 라이프 로그 (LIFE_LOG)
INSERT INTO LIFE_LOG (USER_ID, CONTENT, CATEGORY, LOGGED_AT, ESG_SCORE_EFFECT) VALUES
(1, '아침 출근길에 평소보다 일찍 나와 걸어서 회사에 갔다. 시원한 바람을 느끼며 출근하니 기분이 한결 좋아졌다.', 'E', '2025-10-11 08:10:00', 1.00),
(1, '동료가 어려워하던 엑셀 보고서 정리를 도와주었다. 덕분에 팀 전체 일정이 조금 더 수월하게 조정되었다.', 'S', '2025-10-11 10:40:00', 1.00),
(1, '점심 식사 후 남은 음식 없이 깨끗이 접시를 비웠다. 사소하지만 음식물 쓰레기를 줄이는 데 도움이 되었다.', 'E', '2025-10-11 12:40:00', 1.00),
(1, '업무 중간에 사무실 조명을 점검하고 불필요한 조명을 껐다.', 'E', '2025-10-11 16:30:00', 1.00),
(1, '일요일 오전, 동네 공원에 가서 쓰레기를 주웠다. 아이들이 깨끗한 놀이터에서 놀 수 있으면 좋겠다는 생각이 들었다.', 'S', '2025-10-12 09:50:00', 1.00),
(1, '점심 장을 보러 나가면서 비닐봉투 대신 에코백을 챙겼다.', 'E', '2025-10-12 11:20:00', 1.00),
(1, '이웃이 이사 중이라 잠시 짐 옮기는 걸 도와드렸다. 서로 인사를 나누며 관계가 조금 더 가까워졌다.', 'S', '2025-10-12 14:10:00', 1.00),
(1, '저녁에는 전자기기 전원을 모두 차단하고 조명을 절약했다.', 'E', '2025-10-12 20:20:00', 1.00),
(1, '출근 전 버스 정류장에서 쓰레기를 하나 주워 버렸다. 하루의 시작이 조금 더 보람차게 느껴졌다.', 'E', '2025-10-13 08:00:00', 1.00),
(1, '오전 회의에서 신입사원의 발표를 끝까지 들어주고 피드백을 제공했다.', 'S', '2025-10-13 10:40:00', 1.00),
(1, '점심 후 사무실 환기를 위해 창문을 열어두었다.', 'E', '2025-10-13 13:30:00', 1.00),
(1, '퇴근 전에 모니터와 프린터 전원을 모두 꺼두었다.', 'E', '2025-10-13 18:20:00', 1.00),
(1, '출근길에 지하철을 이용하며 이어폰으로 ESG 관련 팟캐스트를 들었다.', 'E', '2025-10-14 07:50:00', 1.00),
(1, '업무 도중 어려움을 겪는 동료에게 코드를 함께 리뷰해주었다.', 'S', '2025-10-14 15:20:00', 1.00),
(1, '퇴근길에 근처 공원 쓰레기를 몇 개 주워 정리했다.', 'E', '2025-10-14 19:10:00', 1.00),
(1, '오늘은 자동차 대신 자전거로 출근해 아침 공기를 즐겼다.', 'E', '2025-10-15 08:30:00', 1.00),
(1, '회의 중 불필요한 인쇄물을 줄이자는 제안을 했다.', 'E', '2025-10-15 11:10:00', 1.00),
(1, '팀원 생일이라 모두가 함께 축하하며 따뜻한 분위기를 만들었다.', 'S', '2025-10-15 17:00:00', 1.00),
(1, '저녁에는 불필요한 조명을 꺼두고 조용히 책을 읽었다.', 'E', '2025-10-15 20:10:00', 1.00),
(1, '출근길에 대중교통을 이용하며 오늘 일정표를 미리 정리했다.', 'E', '2025-10-16 08:10:00', 1.00),
(1, '업무 중 서버 정리 작업을 맡으며 효율적인 리소스 사용 방안을 제시했다.', 'E', '2025-10-16 11:40:00', 1.00),
(1, '점심 후 회사 근처 화단의 잡초를 뽑아 정리했다.', 'S', '2025-10-16 13:10:00', 1.00),
(1, '저녁에는 회사 문서 정리를 도와주며 팀 정리 문화를 만들었다.', 'S', '2025-10-16 19:20:00', 1.00),
(1, '회의 후 남은 음료를 버리지 않고 정리해두었다.', 'E', '2025-10-17 11:40:00', 1.00),
(1, '회사 내 재활용함이 넘쳐 보이길래 분리 정리를 했다.', 'E', '2025-10-17 14:30:00', 1.00),
(1, '프로젝트 마감으로 야근 중인 팀원에게 간단한 간식을 챙겨줬다.', 'S', '2025-10-17 19:50:00', 1.00),
(1, '주말 아침 동네 산책로를 걸으며 주변 쓰레기를 정리했다.', 'E', '2025-10-18 09:00:00', 1.00),
(1, '오랜만에 가족들과 함께 점심을 먹으며 근황을 나눴다.', 'S', '2025-10-18 12:40:00', 1.00),
(1, '오후에는 에너지 절약 캠페인 관련 영상을 시청했다.', 'E', '2025-10-18 16:00:00', 1.00),
(1, '출근 전에 냉장고 속 유통기한 지난 식품을 정리했다.', 'E', '2025-10-19 08:40:00', 1.00),
(1, '점심시간에 신입사원에게 업무 팁을 알려주며 대화를 나눴다.', 'S', '2025-10-19 13:10:00', 1.00),
(1, '퇴근길에 엘리베이터 대신 계단을 이용했다.', 'E', '2025-10-19 18:30:00', 1.00),
(1, '업무 중 잠시 쉬는 시간에 창문을 열어 환기를 시켰다.', 'E', '2025-10-20 10:00:00', 1.00),
(1, '팀 내부 코드 리뷰 회의에서 후배의 아이디어를 존중하며 피드백을 제공했다.', 'S', '2025-10-20 14:30:00', 1.00),
(1, '퇴근 전 모니터 절전 모드를 확인하고 전체 전원을 껐다.', 'E', '2025-10-20 18:00:00', 1.00),
(1, '저녁에는 가족과 함께 분리수거를 진행했다.', 'E', '2025-10-20 20:30:00', 1.00),
(1, '오늘은 회사 회의에서 종이 인쇄 없이 모두 디지털 문서로 공유했다.', 'E', '2025-10-21 09:40:00', 1.00),
(1, '점심 이후 회의실 불을 끄고 퇴실을 확인했다.', 'E', '2025-10-21 13:50:00', 1.00),
(1, '퇴근길에 길가에 떨어진 종이를 주워 버렸다.', 'E', '2025-10-21 18:20:00', 1.00),
(1, '출근 시 자전거를 타고 사무실까지 이동했다.', 'E', '2025-10-22 08:10:00', 1.00),
(1, '동료가 진행하던 발표 자료를 함께 수정하며 협업했다.', 'S', '2025-10-22 15:00:00', 1.00),
(1, '사무실 내 온도를 점검해 냉난방기를 잠시 껐다.', 'E', '2025-10-23 11:30:00', 1.00),
(1, '퇴근 전 쓰레기통을 비우고 분리수거함을 정리했다.', 'E', '2025-10-23 17:40:00', 1.00),
(1, '퇴근 후 이웃과 함께 단지 주변 청소를 했다.', 'S', '2025-10-23 19:10:00', 1.00),
(1, '대중교통을 이용해 출근하면서 책을 읽으며 시간을 보냈다.', 'E', '2025-10-24 08:00:00', 1.00),
(1, '오후에 동료의 일정 조율을 도와 프로젝트 일정을 맞췄다.', 'S', '2025-10-24 15:30:00', 1.00),
(1, '퇴근 전 모든 콘센트를 점검해 불필요한 전원 연결을 해제했다.', 'E', '2025-10-24 18:40:00', 1.00),
(1, '주말 오전, 동네 하천 주변을 산책하며 떨어진 쓰레기를 주웠다.', 'E', '2025-10-25 09:20:00', 1.00),
(1, '이웃집 아이가 잃어버린 공을 찾아주었다.', 'S', '2025-10-25 13:10:00', 1.00),
(1, '저녁에는 방의 불을 끄고 조명을 최소한으로 유지했다.', 'E', '2025-10-25 20:10:00', 1.00),
(1, '출근 전 재활용 가능한 병과 플라스틱을 분리했다.', 'E', '2025-10-26 08:30:00', 1.00),
(1, '점심시간에 새로 입사한 팀원에게 회사 시스템을 안내해줬다.', 'S', '2025-10-26 12:40:00', 1.00),
(1, '퇴근길에 전철역 근처 벤치 주변 쓰레기를 주웠다.', 'E', '2025-10-26 18:10:00', 1.00),
(1, '회사에서 에너지 절약 관련 교육 영상을 시청했다.', 'E', '2025-10-27 10:00:00', 1.00),
(1, '오후에는 업무로 어려움을 겪는 동료에게 도움을 줬다.', 'S', '2025-10-27 15:10:00', 1.00),
(1, '퇴근 전 모니터 절전 모드를 활성화했다.', 'E', '2025-10-27 18:20:00', 1.00),
(1, '지하철로 출근하며 불필요한 전자기기 사용을 줄였다.', 'E', '2025-10-28 08:00:00', 1.00),
(1, '점심시간에 팀원들과 함께 주변 공원을 산책했다.', 'S', '2025-10-28 13:20:00', 1.00),
(1, '퇴근 후 지역 환경 정화 모임에 참여했다.', 'S', '2025-10-28 19:00:00', 1.00),
(1, '아침에 출근길 버스 안에서 뉴스 대신 환경 관련 칼럼을 읽었다.', 'E', '2025-10-29 08:30:00', 1.00),
(1, '팀 회의 중 서로의 의견을 경청하며 회의 분위기를 좋게 이끌었다.', 'S', '2025-10-29 11:00:00', 1.00),
(1, '퇴근길에 계단을 이용하며 하루를 정리했다.', 'E', '2025-10-29 18:10:00', 1.00),
(1, '출근길에 자전거를 타며 도시의 가을 풍경을 즐겼다.', 'E', '2025-10-30 08:10:00', 1.00),
(1, '회의 후 전자문서로 기록을 남기고 종이 사용을 줄였다.', 'E', '2025-10-30 11:30:00', 1.00),
(1, '업무 중 후배의 질문에 자세히 답해주며 도왔다.', 'S', '2025-10-30 15:40:00', 1.00),
(1, '오늘은 회사 내 쓰레기 분리수거 날이라 함께 정리 작업을 했다.', 'S', '2025-10-31 09:40:00', 1.00),
(1, '점심 이후 불필요한 조명을 껐다.', 'E', '2025-10-31 13:00:00', 1.00),
(1, '퇴근 후 가까운 거리라 걸어서 귀가했다.', 'E', '2025-10-31 18:40:00', 1.00),
(1, '토요일 오전에 동네 공원 쓰레기통 주변을 정리했다.', 'E', '2025-11-01 09:20:00', 1.00),
(1, '오후에는 이웃들과 함께 화단을 가꾸었다.', 'S', '2025-11-01 15:10:00', 1.00),
(1, '일요일이라 가족들과 함께 집 근처를 산책했다.', 'S', '2025-11-02 10:00:00', 1.00),
(1, '산책 중 길가 쓰레기를 주워 버렸다.', 'E', '2025-11-02 10:40:00', 1.00),
(1, '저녁에는 불필요한 콘센트를 뽑아 전력을 절약했다.', 'E', '2025-11-02 20:10:00', 1.00),
(1, '출근길에 버스를 타고 이동하며 스마트폰 대신 독서를 했다.', 'E', '2025-11-03 08:00:00', 1.00),
(1, '업무 중 신입사원의 발표 준비를 도와주었다.', 'S', '2025-11-03 10:40:00', 1.00),
(1, '퇴근 전 전체 모니터 절전 상태를 확인했다.', 'E', '2025-11-03 18:00:00', 1.00),
(1, '출근 시 엘리베이터 대신 계단을 이용했다.', 'E', '2025-11-04 08:30:00', 1.00),
(1, '프로젝트 마감일이라 모두 힘든 하루였지만, 동료를 격려하며 분위기를 지켰다.', 'S', '2025-11-04 15:30:00', 1.00),
(1, '회의실을 나올 때 불필요한 조명을 껐다.', 'E', '2025-11-05 11:10:00', 1.00),
(1, '후배의 보고서 검토를 도와 함께 수정했다.', 'S', '2025-11-05 16:40:00', 1.00),
(1, '오늘은 자동차 대신 지하철로 이동했다.', 'E', '2025-11-06 08:20:00', 1.00),
(1, '오후에는 사무실 화분에 물을 주며 공간을 정리했다.', 'E', '2025-11-06 15:00:00', 1.00),
(1, '업무 후 함께 남은 쓰레기를 정리하며 동료와 대화를 나눴다.', 'S', '2025-11-06 18:40:00', 1.00),
(1, '출근 전 재활용 쓰레기를 분리해 배출했다.', 'E', '2025-11-06 08:00:00', 1.00),
(1, '팀 내 회의 중 서로의 의견을 존중하며 생산적인 논의를 이끌었다.', 'S', '2025-11-06 14:10:00', 1.00),
(1, '퇴근 후 방 전등을 모두 끄고 콘센트를 점검했다.', 'E', '2025-11-06 19:10:00', 1.00);


INSERT INTO QUEST (ADMIN_ID, TITLE, DESCRIPTION, REWARD_SCORE, CATEGORY,`TYPE`, AUTH_TYPE, IS_ACTIVE, MAX_ATTEMPTS, CONDITION_JSON, CREATED_AT) VALUES
-- 🌿 DAILY (10월 1일 ~ 12일)
(1, '텀블러 사용하기', '일회용 컵 대신 텀블러를 사용해보세요.', 10, 'E', 'DAILY', 'IMAGE', TRUE, 1, '{}', '2025-10-01 09:10:00'),
(1, '불 끄기 실천', '외출 시 불을 모두 꺼 에너지를 절약하세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-02 08:30:00'),
(1, '손수건 사용하기', '물티슈 대신 손수건을 사용해보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-03 10:45:00'),
(1, '양치컵 사용하기', '양치할 때 물을 틀어놓지 말고 컵을 사용하세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-04 07:40:00'),
(1, '분리수거 정리하기', '오늘 하루 분리수거를 철저히 실천해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-05 18:10:00'),
(1, '식물에 물주기', '집 안 식물에 물을 주며 마음을 돌보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{"humidity_max": 50}', '2025-10-06 08:30:00'),
(1, '에어컨 온도 유지', '냉방온도를 26도로 유지해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{"temp_min": 26}', '2025-10-07 15:20:00'),
(1, '휴대폰 충전기 뽑기', '사용하지 않는 전자제품 코드를 뽑으세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-08 19:00:00'),
(1, '걷기 실천', '짧은 거리는 걸어서 이동해보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{"pm10_max": 30, "pm25_max": 15}', '2025-10-09 09:40:00'),
(1, '친환경 도시락', '일회용 용기 대신 도시락을 준비해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-10 12:15:00'),
(1, '물 아껴쓰기', '양치나 샤워할 때 물을 아껴 써보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{"humidity_max": 60}', '2025-10-11 10:10:00'),
(1, '휴지 대신 수건 사용하기', '손을 닦을 때 종이 대신 수건을 사용해보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-10-12 08:55:00'),
(1, '텀블러 사용하기', '일회용 컵 대신 텀블러를 사용해보세요.', 10, 'E', 'DAILY', 'IMAGE', TRUE, 1, '{}', '2025-10-31 09:00:00'),
(1, '가벼운 산책', '가까운 공원이나 산책로를 걸으며 마음을 정화하세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{"pm10_max": 40, "pm25_max": 20}', '2025-11-01 10:30:00'),
(1, '리필 제품 이용하기', '리필 가능한 샴푸나 세제를 사용해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-11-02 09:15:00'),
(1, '우산 챙기기', '비 오는 날, 일회용 우비 대신 우산을 사용하세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{"humidity_max": 80}', '2025-11-03 08:50:00'),
(1, '가정 내 전기 점검', '대기전력 기기를 점검하고 콘센트를 뽑아보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-11-04 19:00:00'),
(1, '친구에게 환경 습관 나누기', '지인에게 환경 습관을 한 가지 공유해보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-11-05 13:20:00'),
(1, '잔반 줄이기', '식사 시 음식을 남기지 않도록 노력해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-11-06 12:10:00'),
(1, '교통수단 공유하기', '카풀이나 대중교통을 활용해 출퇴근해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{"pm10_max": 35}', '2025-11-07 08:30:00'),
(1, '에코백 사용하기', '장보거나 외출 시 에코백을 사용해보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{}', '2025-11-08 11:45:00'),
(1, '햇빛으로 빨래 말리기', '건조기 대신 햇빛으로 빨래를 말려보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{"uv_max": 8}', '2025-11-09 10:15:00'),
(1, '가벼운 명상', '자연 속에서 10분간 명상하며 마음의 여유를 찾아보세요.', 10, 'S', 'DAILY', 'TEXT', TRUE, 1, '{"pm10_max": 45}', '2025-11-10 07:40:00'),
(1, '머그컵 사용하기', '카페나 회사에서 일회용 컵 대신 머그컵을 사용해보세요.', 10, 'E', 'DAILY', 'IMAGE', TRUE, 1, '{}', '2025-11-11 09:00:00'),
(1, '물 절약 리마인드', '양치나 세안 시 물을 틀어두지 않고 절약해보세요.', 10, 'E', 'DAILY', 'TEXT', TRUE, 1, '{"humidity_max": 65}', '2025-11-12 08:20:00'),
-- 🌎 WEEKLY (10월 13일 ~ 22일)
(1, '쓰레기 줄이기 도전', '일주일 동안 쓰레기 배출량을 줄여보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 6, '{}', '2025-10-13 08:00:00'),
(1, '중고 나눔하기', '필요 없는 물건을 나누거나 재사용하세요.', 50, 'S', 'WEEKLY', 'TEXT', TRUE, 4, '{}', '2025-10-14 13:40:00'),
(1, '채식 실천하기', '고기 대신 채식을 중심으로 식사해보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 7, '{}', '2025-10-15 11:20:00'),
(1, '자전거 출퇴근하기', '일주일간 자전거로 이동해보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 9, '{"pm10_max": 40, "pm25_max": 20}', '2025-10-16 09:50:00'),
(1, '환경 뉴스 읽기', '매일 환경 관련 뉴스를 하나씩 읽어보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 3, '{"uv_max": 6}', '2025-10-17 14:00:00'),
(1, '물 절약 도전', '샤워 시간을 5분 이하로 줄여보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 8, '{"humidity_max": 55}', '2025-10-18 09:15:00'),
(1, '친환경 장보기', '장바구니를 들고 친환경 매장을 이용하세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 10, '{}', '2025-10-19 10:20:00'),
(1, '플로깅 실천', '산책하며 쓰레기를 주워보세요.', 50, 'S', 'WEEKLY', 'TEXT', TRUE, 5, '{"pm10_max": 35, "pm25_max": 18}', '2025-10-20 18:10:00'),
(1, '전기 절약 캠페인', '대기전력을 줄이는 습관을 실천하세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 4, '{}', '2025-10-21 09:30:00'),
(1, '친환경 요리 만들기', '일회용품 없이 요리를 만들어보세요.', 50, 'S', 'WEEKLY', 'TEXT', TRUE, 7, '{}', '2025-10-22 15:10:00'),
(1, '일주일 텀블러 챌린지', '일주일 동안 외출 시 일회용 컵을 사용하지 마세요.', 50, 'E', 'WEEKLY', 'IMAGE', TRUE, 7, '{}', '2025-11-03 09:00:00'),
(1, '기후 관련 기사 읽기', '하루에 한 번씩 기후 변화 관련 기사를 읽고 생각을 나눠보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 5, '{"temp_min": 5}', '2025-11-04 10:20:00'),
(1, '동네 청소 참여', '거주 지역 근처 쓰레기를 수거해보세요.', 50, 'S', 'WEEKLY', 'TEXT', TRUE, 6, '{"pm10_max": 35, "pm25_max": 18}', '2025-11-05 08:50:00'),
(1, '재활용 정리 주간', '재활용품을 분류하고 올바른 배출법을 실천해보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 8, '{}', '2025-11-06 14:00:00'),
(1, '감사 표현 주간', '주변 사람들에게 감사의 말을 전하고 마음을 나눠보세요.', 50, 'S', 'WEEKLY', 'TEXT', TRUE, 5, '{}', '2025-11-07 11:00:00'),
(1, '걷기+플로깅 챌린지', '하루 20분 이상 걷거나 산책 중 쓰레기를 주워보세요.', 50, 'S', 'WEEKLY', 'TEXT', TRUE, 7, '{"pm10_max": 30}', '2025-11-08 09:15:00'),
(1, '친환경 요리 레시피 공유', '일회용품 없이 채식 위주의 요리를 만들어 공유하세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 7, '{}', '2025-11-09 10:40:00'),
(1, '전자영수증 이용하기', '종이 영수증 대신 전자영수증을 받아보세요.', 50, 'E', 'WEEKLY', 'TEXT', TRUE, 4, '{}', '2025-11-10 08:50:00'),
-- 🌳 SEASON (10월 23일 ~ 31일)
(1, '제로웨이스트 도전', '3개월 동안 플라스틱 사용을 최소화하세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 8, '{}', '2025-10-23 10:00:00'),
(1, '환경 봉사 참여', '환경정화 봉사활동에 참여해보세요.', 200, 'S', 'SEASON', 'TEXT', TRUE, 7, '{"pm10_max": 40, "pm25_max": 20}', '2025-10-24 11:40:00'),
(1, '나무심기 행사', '지역사회 나무심기 캠페인에 참여해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 9, '{"temp_min": 15, "uv_max": 7}', '2025-10-25 09:30:00'),
(1, '친환경 캠페인 홍보', 'SNS로 친환경 캠페인을 홍보하세요.', 200, 'S', 'SEASON', 'TEXT', TRUE, 5, '{}', '2025-10-26 12:00:00'),
(1, '리사이클링 공예', '버려진 물건을 활용해 공예품을 만들어보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 4, '{}', '2025-10-27 14:30:00'),
(1, '에코 챌린지 완주', '모든 주간 퀘스트를 완료해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 10, '{}', '2025-10-28 10:15:00'),
(1, '기후 변화 리포트 작성', '기후 관련 주제로 글을 작성해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 6, '{"temp_min": 10, "pm10_max": 25, "pm25_max": 15}', '2025-10-29 16:20:00'),
(1, '친환경 프로젝트 참여', '단체 프로젝트에 참여해보세요.', 200, 'S', 'SEASON', 'TEXT', TRUE, 9, '{}', '2025-10-30 09:00:00'),
(1, '그린 포인트 챌린지', '11월 동안 환경 관련 퀘스트를 꾸준히 실천해 그린 포인트를 모아보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 10, '{}', '2025-11-01 09:00:00'),
(1, '친환경 캠페인 기획', '친환경 캠페인을 직접 기획하고 온라인으로 공유하세요.', 200, 'S', 'SEASON', 'TEXT', TRUE, 8, '{}', '2025-11-02 12:00:00'),
(1, '텀블러 챌린지', '회사나 외출할 때 일회용 컵을 사용하지 마세요.', 200, 'E', 'WEEKLY', 'IMAGE', TRUE, 10, '{}', '2025-11-03 09:00:00'),
(1, '기후 행동 선언문 작성', '자신만의 기후 실천 선언문을 작성해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 6, '{"temp_min": 5}', '2025-11-03 10:30:00'),
(1, '지역 친환경 가게 방문', '지역 내 친환경 매장이나 제로웨이스트숍을 방문해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 7, '{}', '2025-11-04 15:00:00'),
(1, 'ESG 팀 프로젝트 참여', '다른 사용자들과 함께 ESG 주제의 미니 프로젝트를 진행해보세요.', 200, 'S', 'SEASON', 'TEXT', TRUE, 10, '{}', '2025-11-05 13:40:00'),
(1, '기후 변화 리서치', '온도, 미세먼지, 습도 데이터를 조사하여 보고서를 작성하세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 9, '{"temp_min": 3, "pm10_max": 30, "pm25_max": 15}', '2025-11-06 09:20:00'),
(1, '나무 돌보기', '집 앞이나 공원의 나무에 물을 주고 쓰레기를 치워보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 5, '{"humidity_max": 55}', '2025-11-07 11:00:00'),
(1, '제로웨이스트 주간 챌린지', '11월 둘째 주 동안 일회용품 사용을 최소화해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 8, '{}', '2025-11-08 09:00:00'),
(1, '에코 크리에이터 활동', '환경 주제를 담은 콘텐츠(사진/영상)를 만들어 공유하세요.', 200, 'S', 'SEASON', 'TEXT', TRUE, 6, '{}', '2025-11-10 15:30:00'),
(1, '탄소중립 실천 보고서', '한 달 동안의 탄소 절감 실천 내역을 보고서로 작성해보세요.', 200, 'E', 'SEASON', 'TEXT', TRUE, 10, '{"temp_min": 5, "pm10_max": 25}', '2025-11-12 10:00:00');

-- 사용자 퀘스트 (USER_QUEST)
INSERT INTO USER_QUEST (USER_ID, QUEST_ID, STATUS, ATTEMPT_COUNT, STARTED_AT, COMPLETED_AT) VALUES
(1, 1, 'PENDING', 1, '2025-10-25 20:00:00', NULL);

-- 인증 (CERTIFICATION)
INSERT INTO CERTIFICATION ( UQ_ID, USER_ID, AUTH_TYPE, AUTH_CONTENT, VALIDATION_STATUS, VALIDATED_AT, CREATED_AT, MODEL_TYPE, CONFIDENCE_SCORE) VALUES
(1, 1, 'IMAGE', '/uploads/auth_type/locknlock.png', 'PENDING', NULL, NOW(), 'HUGGING', 50.00);

-- 칭호 (TITLE)
INSERT INTO TITLE (NAME, DESCRIPTION, CONDITION_JSON) VALUES
('텀블러 새싹', '첫 텀블러 사용 인증에 성공하면 획득해요!', '{"questTitle": "텀블러 사용하기", "count": 1}'),
('작은 실천가', '텀블러 사용 10회 인증에 성공하면 획득해요!', '{"questTitle": "텀블러 사용하기", "count": 10}'),
('꾸준함의 증표', '일주일 연속으로 텀블러 사용을 인증하면 획득해요!', '{"questTitle": "텀블러 사용하기", "count": 20}'),
('텀블러 생활자', '텀블러 사용 50회 인증! 이제 당신의 일상이 되었네요.', '{"questTitle": "텀블러 사용하기", "count": 50}'),
('텀블러 장인', '텀블러 사용 100회 인증! 장인의 경지에 올랐습니다.', '{"questTitle": "텀블러 사용하기", "count": 100}'),
('선한 영향력', '나의 텀블러 인증을 10회 이상 공유하여 선한 영향력을 보여주세요!', '{"questTitle": "텀블러 사용하기", "count": 10}'),
('컵 컬렉터', '3종류 이상의 다른 컵/텀블러로 인증에 성공하면 획득해요!', '{"questTitle": "텀블러 사용하기", "count": 3}'),
('지구를 구하는 한 잔', '텀블러 사용 365회 인증! 당신의 한 잔이 지구를 구합니다.', '{"questTitle": "텀블러 사용하기", "count": 365}'),
('AI가 인정한 모범생', 'AI 인증을 30회 연속으로 한 번에 통과하면 획득해요!', '{"questTitle": "AI 인증", "count": 30}');


-- 사용자 칭호 (USER_TITLE)
INSERT INTO USER_TITLE (USER_ID, TITLE_ID, EARNED_AT,  IS_MAIN) VALUES
(1, 3, '2025-10-01 09:00:00', TRUE);

-- 회사 (COMPANY)
INSERT INTO COMPANY (COMPANY_NAME, COMPANY_LOGO, COMPANY_WEBSITE_URL, CREATED_AT, UPDATED_AT) VALUES
('삼성', '/uploads/logo/samsung.png', 'https://www.samsung.com/sec/sustainability/main/', '2025-05-02 09:30:00', '2025-05-15 10:45:00'),
('현대자동차', '/uploads/logo/현대자동차.png', 'https://www.hyundaigroup.com/social/report.asp', '2025-05-10 11:20:00', '2025-05-28 14:50:00'),
('포스코', '/uploads/logo/포스코.png', 'https://sustainability.posco.com/S91/S91F10/kor/cmspage.do?mmcd=1745996979005381', '2025-06-01 10:00:00', '2025-06-15 09:10:00'),
('대한항공', '/uploads/logo/대한항공.png', 'https://www.koreanair.com/contents/footer/about-us/sustainable-management/report', '2025-06-08 13:40:00', '2025-06-22 16:20:00'),
('LG에너지솔루션', '/uploads/logo/LG에너지솔루션.png', 'https://www.lgensol.com/kr/esg-sustainability', '2025-06-25 09:10:00', '2025-07-05 17:00:00'),
('기술보증기금', '/uploads/logo/기술보증기금.png', 'https://www.kibo.or.kr/main/work/about041004.do', '2025-07-03 11:25:00', '2025-07-17 08:45:00'),
('신세계', '/uploads/logo/신세계.png', 'https://www.shinsegae.com/company/esg/esg_report.do', '2025-07-15 14:30:00', '2025-07-27 19:50:00'),
('아시아나항공', '/uploads/logo/아시아나항공.png', 'https://flyasiana.com/C/KR/KO/contents/sustainability-report-esg1', '2025-07-22 09:40:00', NULL),
('에코프로머티', '/uploads/logo/에코프로머티.png', 'https://www.ecopromaterials.com/sub0407', '2025-08-02 08:50:00', NULL),
('한전KPS', '/uploads/logo/한전KPS.png', 'https://www.kps.co.kr/esg/esg_01_05.do', '2025-08-12 13:30:00', NULL),
('한국가스공사', '/uploads/logo/한국가스공사.png', 'https://www.kogas.or.kr/site/koGas/1050904010000#', '2025-08-18 10:15:00', NULL),
('코웨이', '/uploads/logo/코웨이.png', 'https://sustainability.coway.co.kr/', '2025-09-01 11:00:00', NULL),
('이마트', '/uploads/logo/이마트.png', 'https://company.emart.com/ko/ethic/sustainability_report.do', '2025-09-08 15:20:00', NULL),
('NH농협생명', '/uploads/logo/NH농협생명.png', 'https://www.nhlife.co.kr/ho/ci/HOCI0052M00.nhl', '2025-09-18 09:50:00', NULL);

-- ESG 카테고리 (ESG_CATEGORY)
INSERT INTO ESG_CATEGORY ( CATEGORY_NAME) VALUES
('LEADER'),
('SPONSOR');


-- 회사-카테고리 매핑 (COMPANY_CATEGORY)
INSERT INTO COMPANY_CATEGORY (COMPANY_ID, CATEGORY_ID) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(8, 1),
(9, 2),
(10, 2),
(11, 2),
(12, 2),
(13, 2),
(14, 2);

-- 인증 코드 (AUTH_CODE) 
INSERT INTO AUTH_CODE (EMAIL, AUTH_CODE, EXPIRY_TIME, IS_VERIFIED, CREATED_AT) VALUES
('matcha@google.com', 'A1B2C3', DATE_ADD(NOW(), INTERVAL 5 MINUTE), false, NOW());

-- 약관 (TERMS)
INSERT INTO TERMS (TITLE, VERSION, CONTENT, IS_REQUIRED, CREATED_AT) VALUES
('서비스 이용약관', '1.0', '안녕하세요! Matacha World에 오신 것을 환영합니다. 본 약관은 저희 서비스를 이용하는 모든 분들을 위한 약속입니다.
- 서비스 이용: 여러분은 Matacha World를 통해 일상 속 ESG 활동을 기록하고, 재미있는 퀘스트에 참여하며 선한 영향력을 나눌 수 있습니다.
- 게시물 책임과 권리: 서비스에 올리는 사진이나 글(게시물)의 책임과 권리는 회원님 본인에게 있습니다. 다만, 저희는 서비스를 운영하고 홍보하기 위해 회원님의 게시물을 사용할 수 있습니다. 다른 사람의 권리를 침해하는 게시물은 올릴 수 없습니다.
- 이것만은 지켜주세요: 다른 사람을 비방하거나, 허위 사실을 유포하거나, 상업적 광고를 하는 등 서비스의 좋은 분위기를 해치는 행동은 금지됩니다. 위반 시 서비스 이용이 제한될 수 있습니다.
- 서비스의 역할과 한계: 저희는 더 나은 서비스를 제공하기 위해 노력하지만, 서비스가 일시적으로 중단되거나 내용이 변경될 수 있습니다. AI 분석 결과는 100% 정확하지 않을 수 있으며, 회사는 회원이 올린 게시물의 내용을 보증하지 않습니다.
개인정보는 개인정보 처리방침에 따라 안전하게 관리됩니다. 약관은 변경될 수 있으며, 변경 시에는 미리 알려드립니다. 즐거운 ESG 실천, Matacha World와 함께해요!', true, NOW());

-- 사용자 약관 동의 (USER_TERMS)
INSERT INTO USER_TERMS (USER_ID, TERM_ID, IS_AGREED, AGREED_AT) VALUES
(1, 1, true, NOW());

-- =================================================================
-- ✅ DB 권한 설정 (Access denied 오류 해결을 위한 필수 코드)
-- =================================================================

-- 'root' 계정이 클러스터 내부의 모든 IP 주소(%)에서 접속을 허용하도록 권한을 부여합니다.
-- '1234'는 고객님의 DB 비밀번호입니다. (ConfigMap의 DB_PASSWORD와 일치해야 함)
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '1234' WITH GRANT OPTION;
FLUSH PRIVILEGES;