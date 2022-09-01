package com.hanghae0705.sbmoney.model.domain.item;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hanghae0705.sbmoney.model.domain.user.User;
import com.hanghae0705.sbmoney.model.domain.baseEntity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
public class GoalItem extends BaseEntity {
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_ID")
    @JsonBackReference(value = "user-fk")
    private User user;

    @Column
    private LocalDateTime reachedAt;

    @Column
    private String image;

    @Column(nullable = false)
    private int count;

    @Column(nullable = false)
    private int total;

    @OneToOne
    @JoinColumn(name = "ITEM_ID")
    @JsonBackReference(value = "item-fk")
    private Item item;

    @Column
    private boolean checkReached;

    @Column
    private double goalPercent;

    public GoalItem(User user, int count, int total, Item item) {
        this.user = user;
        this.count = count;
        this.total = total;
        this.item = item;
    }


    public void setCheckReached(boolean checkReached, double goalPercent, LocalDateTime reachedAt){
        this.checkReached = checkReached;
        this.goalPercent = goalPercent;
        this.reachedAt = reachedAt;
    }

    public void setCheckReached(boolean checkReached, LocalDateTime reachedAt){
        this.checkReached = checkReached;
        this.reachedAt = reachedAt;
    }

    public void setGoalPercent(double goalPercent){
        this.goalPercent = goalPercent;
    }

    public void setImage(String image){
        this.image = image;
    }

    public void updateGoalItem(int count, int total, double goalPercent){
        this.count = count;
        this.total = total;
        this.goalPercent = goalPercent;
    }

    public void updateGoalItem(int count, int total, Item item, double goalPercent) {
        this.count = count;
        this.total = total;
        this.item = item;
        this.goalPercent = goalPercent;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long categoryId;
        private Long itemId;
        private int goalItemCount;
        private int price;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckGoalItem {
        private Long goalItemId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateRequest {
        private int goalItemCount;
        private int price;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Response {
        private Long goalItemId;
        private Long categoryId;
        private String categoryName;
        private Long itemId;
        private String itemName;
        private int goalItemCount;
        private int price;
        private int totalPrice;
        private boolean checkReached;
        private double goalPercent;
        private int savedItemCount;
        private LocalDateTime createdAt;
        private LocalDateTime reachedAt;
        private String image;

        public Response(GoalItem goalItem){
            this.goalItemId = goalItem.getId();
            this.categoryId = goalItem.getItem().getCategory().getId();
            this.categoryName = goalItem.getItem().getCategory().getName();
            this.itemId = goalItem.getItem().getId();
            this.itemName = goalItem.getItem().getName();
            this.goalItemCount = goalItem.getCount();
            this.price = (goalItem.getCount() == 0)? 0 : goalItem.getTotal() / goalItem.getCount();
            this.totalPrice = goalItem.getTotal();
            this.checkReached = goalItem.isCheckReached();
            this.goalPercent = goalItem.getGoalPercent();
            this.createdAt = goalItem.getCreatedDate();
            this.reachedAt = goalItem.getReachedAt();
            this.image = goalItem.getImage();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class AllResponse {
        private Long goalItemId;
        private Long categoryId;
        private String categoryName;
        private Long itemId;
        private String itemName;
        private int goalItemCount;
        private int price;
        private int totalPrice;
        private boolean checkReached;
        private double goalPercent;
        private int savedItemCount;
        private LocalDateTime createdAt;
        private LocalDateTime reachedAt;
        private String image;
        private List<SavedItem.Response> savedItemResponses;

        public AllResponse(GoalItem.Response goalItemResponse, List<SavedItem.Response> savedItemResponses){
            this.goalItemId = goalItemResponse.getGoalItemId();
            this.categoryId = goalItemResponse.getCategoryId();
            this.categoryName = goalItemResponse.getCategoryName();
            this.itemId = goalItemResponse.getItemId();
            this.itemName = goalItemResponse.getItemName();
            this.goalItemCount = getGoalItemCount();
            this.price = goalItemResponse.getPrice();
            this.totalPrice = goalItemResponse.getTotalPrice();
            this.checkReached = goalItemResponse.isCheckReached();
            this.goalPercent = goalItemResponse.getGoalPercent();
            this.savedItemCount = goalItemResponse.getSavedItemCount();
            this.createdAt = goalItemResponse.getCreatedAt();
            this.reachedAt = goalItemResponse.getReachedAt();
            this.image = goalItemResponse.getImage();
            this.savedItemResponses = savedItemResponses;
        }

        public AllResponse(GoalItem goalItem, List<SavedItem.Response> savedItemResponses){
            this.goalItemId = goalItem.getId();
            this.categoryId = goalItem.getItem().getCategory().getId();
            this.categoryName = goalItem.getItem().getCategory().getName();
            this.itemId = goalItem.getItem().getId();
            this.itemName = goalItem.getItem().getName();
            this.goalItemCount = goalItem.getCount();
            this.price = (goalItem.getCount() == 0)? 0 : goalItem.getTotal() / goalItem.getCount();
            this.totalPrice = goalItem.getTotal();
            this.checkReached = goalItem.isCheckReached();
            this.goalPercent = goalItem.getGoalPercent();
            this.createdAt = goalItem.getCreatedDate();
            this.reachedAt = goalItem.getReachedAt();
            this.image = goalItem.getImage();
            this.savedItemResponses = savedItemResponses;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class HistoryResponse {
        private Long goalItemId;
        private Long categoryId;
        private String categoryName;
        private Long itemId;
        private String itemName;
        private int goalItemCount;
        private int price;
        private int totalPrice;
        private boolean checkReached;
        private double goalPercent;
        private int savedItemCount;
        private LocalDateTime createdAt;
        private LocalDateTime reachedAt;
        private List<SavedItem.Response> savedItems;

        public HistoryResponse(GoalItem goalItem, int totalPrice, List<SavedItem.Response> savedItems){
            this.goalItemId = goalItem.getId();
            this.categoryId = goalItem.getItem().getCategory().getId();
            this.categoryName = goalItem.getItem().getCategory().getName();
            this.itemId = goalItem.getItem().getId();
            this.itemName = goalItem.getItem().getName();
            this.goalItemCount = goalItem.getCount();
            this.price = (goalItem.getCount() == 0)? 0 : goalItem.getTotal() / goalItem.getCount();
            this.totalPrice = totalPrice;
            this.checkReached = goalItem.isCheckReached();
            this.goalPercent = goalItem.getGoalPercent();
            this.createdAt = goalItem.getCreatedDate();
            this.reachedAt = goalItem.getReachedAt();
            this.savedItems = savedItems;
        }
    }


}
