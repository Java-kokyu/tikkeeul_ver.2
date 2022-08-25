package com.hanghae0705.sbmoney.model.domain.statistic;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.IdentityHashMap;

@Entity
@RequiredArgsConstructor
@Getter
public class StatisticsMyMonth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int rankCnt;

    @Column(nullable = false)
    private int rankPrice;

    @Column(nullable = false)
    private Long totalCnt;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private String standardDate;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long categoryId;

    @Builder
    public StatisticsMyMonth(Long userId, String standardDate, String itemName, int totalPrice, Long totalCnt, int rankPrice, int rankCnt, Long categoryId) {
        this.userId = userId;
        this.standardDate = standardDate;
        this.itemName = itemName;
        this.totalPrice = totalPrice;
        this.totalCnt = totalCnt;
        this.rankPrice = rankPrice;
        this.rankCnt = rankCnt;
        this.categoryId = categoryId;
    }

    @Getter
    public static class StatisticsMonthByPrice {
        private final Long userId;
        private final String itemName;
        private final int rank;
        private final Long categoryId;

        @Builder
        public StatisticsMonthByPrice(Long userId, String itemName, int rankPrice, Long categoryId) {
            this.userId = userId;
            this.itemName = itemName;
            this.rank = rankPrice;
            this.categoryId = categoryId;
        }
    }

    @Getter
    public static class StatisticsMonthByCnt {
        private final Long userId;
        private final String itemName;
        private final int rank;
        private final Long categoryId;

        @Builder
        public StatisticsMonthByCnt(Long userId, String itemName, int rankCnt, Long categoryId) {
            this.userId = userId;
            this.itemName = itemName;
            this.rank = rankCnt;
            this.categoryId = categoryId;
        }
    }
    public void changeRankCount(int rank) {
        this.rankCnt = rank;
    }
}
