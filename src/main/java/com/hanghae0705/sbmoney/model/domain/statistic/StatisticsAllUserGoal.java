package com.hanghae0705.sbmoney.model.domain.statistic;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@RequiredArgsConstructor
public class StatisticsAllUserGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String standardDate;

    @NotNull
    private Long categoryId;

    @NotNull
    private String itemName;

    @NotNull
    private int totalPrice;

    @NotNull
    private Long totalCount;

    private int rankPrice;
    private int rankCount;

    @Builder
    public StatisticsAllUserGoal(long id, String standardDate, Long categoryId, String itemName, int totalPrice, Long totalCount, int rankPrice, int rankCount) {
        this.id = id;
        this.standardDate = standardDate;
        this.categoryId = categoryId;
        this.itemName = itemName;
        this.totalPrice = totalPrice;
        this.totalCount = totalCount;
        this.rankPrice = rankPrice;
        this.rankCount = rankCount;
    }

    public void changeRankCount(int rank){
        this.rankCount = rank;
    }

    @Getter
    public static class GoalByPrice {
        private final Long categoryId;
        private final String itemName;
        private final int rank;

        @Builder
        public GoalByPrice(String itemName, Long categoryId, int rankPrice) {
            this.itemName = itemName;
            this.categoryId = categoryId;
            this.rank = rankPrice;
        }
    }

    @Getter
    public static class GoalByCount {
        private final Long categoryId;
        private final String itemName;
        private final int rank;

        @Builder
        public GoalByCount(String itemName, Long categoryId, int rankCount) {
            this.itemName = itemName;
            this.categoryId = categoryId;
            this.rank = rankCount;
        }
    }
}
