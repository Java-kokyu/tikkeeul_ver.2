package com.hanghae0705.sbmoney.model.domain.item;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hanghae0705.sbmoney.model.domain.baseEntity.CreatedDate;
import com.hanghae0705.sbmoney.model.domain.user.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
public class SavedItem extends CreatedDate {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @JsonBackReference(value = "user-fk")
    private User user;

    @Column(nullable = false)
    private int price;

    @OneToOne
    @JoinColumn(name = "ITEM_ID")
    @JsonBackReference(value = "item-fk")
    private Item item;

    public SavedItem(Item item, int price, User user){
        this.item = item;
        this.price = price;
        this.user = user;
    }


    public void update(int price){
        this.price = price;
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private Long itemId;
        private int price;
    }
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Update {
        private int price;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long savedItemId;
        private LocalDateTime createdDate;
        private Long categoryId;
        private String categoryName;
        private String itemName;
        private int price;

        public Response(SavedItem savedItem) {
            this.savedItemId = savedItem.getId();
            this.createdDate = savedItem.getCreatedDate();
            this.categoryId = savedItem.getItem().getCategory().getId();
            this.categoryName = savedItem.getItem().getCategory().getName();
            this.itemName = savedItem.getItem().getName();
            this.price = savedItem.getPrice();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class IntegrateResponse {
        private int total;
        private List<Response> responseList;

        public IntegrateResponse(int total, List<Response> responseList) {
            this.total = total;
            this.responseList = responseList;
        }
    }

}
