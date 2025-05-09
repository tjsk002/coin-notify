package com.coin_notify.coin_notify.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("markets")
public class MarketEntity extends BasicEntity {
    @Id
    private Long id;

    @Column("market_code")
    private String marketCode;

    @Column("korean_name")
    private String koreanName;

    @Column("english_name")
    private String englishName;

    @Override
    public String toString() {
        return "데이터 확인 : {" + "marketCode=" + marketCode +  ", koreanName=" + koreanName + '}';
    }
}
