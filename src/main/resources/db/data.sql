CREATE TABLE coins
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) UNIQUE,
    korean_name  VARCHAR(255),
    english_name VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP DEFAULT NULL
);

CREATE TABLE markets
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) UNIQUE,
    korean_name  VARCHAR(255),
    english_name VARCHAR(255),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at   TIMESTAMP DEFAULT NULL
);

CREATE TABLE real_time_prices
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
