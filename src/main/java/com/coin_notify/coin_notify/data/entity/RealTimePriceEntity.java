package com.coin_notify.coin_notify.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("real_time_prices")
public class RealTimePriceEntity extends BasicEntity {
	@Id
	private Long id;

	@Column("market_id")
	private Long marketId;

	@Column("trade_date") // 최근 거래 일자 (UTC)
	private String tradeDate;

	@Column("trade_time") // 최근 거래 시각 (UTC)
	private String tradeTime;

	@Column("trade_date_kst") // 최근 거래 일자 (KST)
	private String tradeDateKst;

	@Column("trade_time_kst") // 최근 거래 시각 (KST)
	private String tradeTimeKst;

	@Column("trade_timestamp") // 최근 거래 일시 (Unix Timestamp)
	private Long tradeTimestamp;

	@Column("opening_price") // 시가
	private Double openingPrice;

	@Column("high_price") // 고가
	private Double highPrice;

	@Column("low_price") // 저가
	private Double lowPrice;

	@Column("trade_price") // 종가 (현재가)
	private Double tradePrice;

	@Column("prev_closing_price") // 전일 종가
	private Double prevClosingPrice;

	@Column("change") // 상승/하락/보합
	private String change;

	@Column("change_price") // 변화액
	private Double changePrice;

	@Column("change_rate") // 변화율
	private Double changeRate;

	@Column("trade_volume") // 최근 거래량
	private Double tradeVolume;

	@Column("acc_trade_price") // 누적 거래대금 (UTC 0시 기준)
	private Double accTradePrice;

	@Column("acc_trade_price_24h") // 24시간 누적 거래대금
	private Double accTradePrice24h;

	@Column("acc_trade_volume") // 누적 거래량 (UTC 0시 기준)
	private Double accTradeVolume;

	@Column("acc_trade_volume_24h") // 24시간 누적 거래량
	private Double accTradeVolume24h;

	@Column("timestamp")
	private Long timestamp;
}
