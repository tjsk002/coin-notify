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

    @Column("name")
    private String name;

    @Column("korean_name")
    private String koreanName;

    @Column("english_name")
    private String englishName;
}
