package com.hanghae0705.sbmoney.model.domain.item;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hanghae0705.sbmoney.model.domain.user.Favorite;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@NoArgsConstructor
public class Item {

    //insert into item values (1, 3000, '소세지빵', 1);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    private int defaultPrice;

    @NotNull
    @ManyToOne
    @JoinColumn(name="CATEGORY_ID")
    @JsonBackReference(value = "item-category-fk")
    private Category category;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Long categoryId;
        private String categoryName;
        private Long itemId;
        private String itemName;
        private int itemDefaultPrice;

        public Response(Item item){
            this.categoryId = item.getCategory().getId();
            this.categoryName = item.getCategory().getName();
            this.itemId = item.getId();
            this.itemName = item.getName();
            this.itemDefaultPrice = item.getDefaultPrice();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Request {
        private Long categoryId;
        private String itemName;
        private int defaultPrice;
    }

    public Item(Request request, Category category) {
        this.name = request.getItemName();
        this.category = category;
        this.defaultPrice = request.getDefaultPrice();
    }

    public Item(Favorite.Request favoriteRequest, Category category) {
        this.name = favoriteRequest.getItemName();
        this.category = category;
        this.defaultPrice = favoriteRequest.getPrice();
    }

}
