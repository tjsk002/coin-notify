CREATE TABLE IF NOT EXISTS coins
(
    id           SERIAL PRIMARY KEY,
    symbol       VARCHAR(255) UNIQUE,
    korean_name  VARCHAR(255),
    english_name VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS markets
(
    id           SERIAL PRIMARY KEY,
    market_code  VARCHAR(255) UNIQUE,
    korean_name  VARCHAR(255),
    english_name VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS real_time_prices
(
    id                   SERIAL PRIMARY KEY,                  -- 자동 증가하는 고유 ID
    market_id            BIGINT     NOT NULL,                 -- 코인 식별자 (coins 테이블의 PK)
    trade_date           VARCHAR(8) NOT NULL,                 -- 최근 거래 일자 (UTC)
    trade_time           VARCHAR(6) NOT NULL,                 -- 최근 거래 시각 (UTC)
    trade_date_kst       VARCHAR(8) NOT NULL,                 -- 최근 거래 일자 (KST)
    trade_time_kst       VARCHAR(6) NOT NULL,                 -- 최근 거래 시각 (KST)
    trade_timestamp      BIGINT     NOT NULL,                 -- 최근 거래 일시 (Unix Timestamp)
    opening_price        DOUBLE PRECISION,                    -- 시가
    high_price           DOUBLE PRECISION,                    -- 고가
    low_price            DOUBLE PRECISION,                    -- 저가
    trade_price          DOUBLE PRECISION,                    -- 종가 (현재가)
    prev_closing_price   DOUBLE PRECISION,                    -- 전일 종가
    change               VARCHAR(4),                          -- 상승/하락/보합
    change_price         DOUBLE PRECISION,                    -- 변화액
    change_rate          DOUBLE PRECISION,                    -- 변화율
    trade_volume         DOUBLE PRECISION,                    -- 가장 최근 거래량
    acc_trade_price      DOUBLE PRECISION,                    -- 누적 거래대금 (UTC 0시 기준)
    acc_trade_price_24h  DOUBLE PRECISION,                    -- 24시간 누적 거래대금
    acc_trade_volume     DOUBLE PRECISION,                    -- 누적 거래량 (UTC 0시 기준)
    acc_trade_volume_24h DOUBLE PRECISION,                    -- 24시간 누적 거래량
    timestamp            BIGINT     NOT NULL,                 -- API 응답 시점 타임스탬프
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 일시
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 수정 일시
    deleted_at           TIMESTAMP DEFAULT NULL,              -- 삭제 일시
    CONSTRAINT fk_market FOREIGN KEY (market_id)
        REFERENCES markets (id)                               -- 외래키 제약조건
);

CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, -- 사용자 ID, 자동 증가
    uuid       UUID NOT NULL,                                   -- UUID, 고유 식별자
    user_agent varchar(255),                                    -- 사용자 에이전트 (User-Agent), 사용자의 브라우저 및 장치 정보
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,           -- 생성 일시
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,           -- 수정 일시
    deleted_at TIMESTAMPTZ DEFAULT NULL,                        -- 삭제 일시
    CONSTRAINT unique_uuid UNIQUE (uuid)                        -- UUID
);

CREATE TABLE IF NOT EXISTS like_coins
(
    id         SERIAL PRIMARY KEY,
    user_id    BIGINT  NOT NULL,                           -- 사용자 ID
    coin_id    BIGINT  NOT NULL,                           -- 코인 심볼
    is_active  BOOLEAN NOT NULL DEFAULT FALSE,             -- 좋아요 활성화
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP, -- 생성 일시
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP, -- 수정 일시
    deleted_at TIMESTAMP        DEFAULT NULL,              -- 삭제 일시
    UNIQUE (user_id, coin_id),                             -- 중복 저장 방지
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,           -- users 테이블에 FK 연결
    CONSTRAINT fk_coin FOREIGN KEY (coin_id)
        REFERENCES coins (id) ON DELETE CASCADE            -- coins 테이블에 FK 연결
);

CREATE TABLE IF NOT EXISTS like_markets
(
    id         SERIAL PRIMARY KEY,
    user_id    BIGINT  NOT NULL,                           -- 사용자 ID
    market_id  BIGINT  NOT NULL,                           -- 마켓 이름 (예: 'KRW-BTC' 등)
    is_active  BOOLEAN NOT NULL DEFAULT FALSE,             -- 좋아요 활성화
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP, -- 생성 일시
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP, -- 수정 일시
    deleted_at TIMESTAMP        DEFAULT NULL,              -- 삭제 일시
    UNIQUE (user_id, market_id),                           -- 중복 저장 방지
    CONSTRAINT fk_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,           -- users 테이블에 FK 연결
    CONSTRAINT fk_market FOREIGN KEY (market_id)
        REFERENCES markets (id) ON DELETE CASCADE          -- markets 테이블에 FK 연결
);

CREATE TABLE IF NOT EXISTS notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    market_id  BIGINT NOT NULL,
    log        TEXT   NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (market_id) REFERENCES markets (id) ON DELETE CASCADE
);