package com.hanghae0705.sbmoney.model.domain.item;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hanghae0705.sbmoney.model.domain.baseEntity.CreatedDate;
import com.hanghae0705.sbmoney.model.domain.user.User;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
        private LocalDateTime createdDate;
        private Long categoryId;
        private String categoryName;
        private Long itemId;
        private String itemName;
        private int itemDefaultPrice;

        public Response(SavedItem savedItem) {
            this.createdDate = savedItem.getCreatedDate();
            this.categoryId = savedItem.getItem().getCategory().getId();
            this.categoryName = savedItem.getItem().getCategory().getName();
            this.itemId = savedItem.getItem().getId();
            this.itemName = savedItem.getItem().getName();
            this.itemDefaultPrice = savedItem.getPrice();
        }
    }

}
