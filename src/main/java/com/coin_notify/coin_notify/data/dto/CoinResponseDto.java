package com.coin_notify.coin_notify.data.dto;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoinResponseDto {
    private final Long id;
    private final String symbol;
    private final String koreanName;
    private final String englishName;
    private final String createdAt;
    private LikeCoinDto likeCoins;

    public CoinResponseDto(CoinEntity entity, LikeCoinDto likeCoinDto) {
        this.id = entity.getId();
        this.symbol = entity.getSymbol();
        this.koreanName = entity.getKoreanName();
        this.englishName = entity.getEnglishName();
        this.createdAt = entity.getCreatedAt().toString();
        this.likeCoins = likeCoinDto;
    }
}
