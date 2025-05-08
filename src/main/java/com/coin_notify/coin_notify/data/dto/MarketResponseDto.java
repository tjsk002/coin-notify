package com.coin_notify.coin_notify.data.dto;

import com.coin_notify.coin_notify.data.entity.MarketEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketResponseDto {
    private final Long id;
    private final String market;
    private final String koreanName;
    private final String englishName;
    private final String createdAt;
    private LikeMarketDto likeMarkets;

    public MarketResponseDto(MarketEntity entity, LikeMarketDto likeMarketDto) {
        this.id = entity.getId();
        this.market = entity.getMarketCode();
        this.koreanName = entity.getKoreanName();
        this.englishName = entity.getEnglishName();
        this.createdAt = entity.getCreatedAt().toString();
        this.likeMarkets = likeMarketDto;
    }
}
