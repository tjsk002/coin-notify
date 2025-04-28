package com.coin_notify.coin_notify.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("coins")
public class CoinEntity extends BasicEntity {
    @Id
    private Long id;

    @Column("symbol")
    private String symbol;

    @Column("korean_name")
    private String koreanName;

    @Column("english_name")
    private String englishName;

    @Column("like_count")
    private Integer likeCount;
}
