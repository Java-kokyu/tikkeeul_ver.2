package com.hanghae0705.sbmoney.service.item;

import com.hanghae0705.sbmoney.data.Message;
import com.hanghae0705.sbmoney.data.ResponseMessage;
import com.hanghae0705.sbmoney.exception.ItemException;
import com.hanghae0705.sbmoney.model.domain.item.GoalItem;
import com.hanghae0705.sbmoney.model.domain.item.Item;
import com.hanghae0705.sbmoney.model.domain.item.SavedItem;
import com.hanghae0705.sbmoney.model.domain.user.User;
import com.hanghae0705.sbmoney.repository.item.FavoriteRepository;
import com.hanghae0705.sbmoney.repository.item.SavedItemRepository;
import com.hanghae0705.sbmoney.util.MathFloor;
import com.hanghae0705.sbmoney.validator.ItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedItemService {
    private final SavedItemRepository savedItemRepository;
    private final ItemValidator itemValidator;

    public static int getTotalPrice(User user) {
        int total = 0;
        List<SavedItem> savedItems = user.getSavedItems();
        for(SavedItem savedItem : savedItems) {
            total += savedItem.getPrice();
        }
        return total;
    }

    @Transactional
    public ResponseMessage postSavedItem(SavedItem.Request savedItemRequest, User user) throws ItemException {
        Item item = itemValidator.isValidItem(savedItemRequest.getItemId());
        int price = (savedItemRequest.getPrice() == 0) ? item.getDefaultPrice() : savedItemRequest.getPrice();
        itemValidator.isValidPrice(savedItemRequest.getPrice());

        savedItemRepository.save(new SavedItem(item, price, user));

        return ResponseMessage.builder()
                .msg("티끌 등록에 성공하였습니다.")
                .build();
    }

    public ResponseMessage getSavedItems(User user) {
        List<SavedItem> savedItemList = user.getSavedItems();
        List<SavedItem.Response> savedItemResponseList = new ArrayList<>();
        for (SavedItem savedItem : savedItemList) {
            SavedItem.Response savedItemResponse = new SavedItem.Response(savedItem);
            savedItemResponseList.add(savedItemResponse);
        }
        Collections.reverse(savedItemResponseList); //id 내림차순 정렬
        return ResponseMessage.builder()
                .msg("티끌 조회에 성공하였습니다.")
                .data(savedItemResponseList)
                .build();
    }

    @Transactional
    public ResponseMessage updateSavedItem(Long savedItemId, SavedItem.Update price, User user) throws ItemException {
        SavedItem savedItem = itemValidator.isValidSavedItem(savedItemId, user);
        itemValidator.isValidPrice(price.getPrice());
        int updatePrice = getTotalPrice(user) + price.getPrice();

        savedItem.update(updatePrice);

        return ResponseMessage.builder()
                .msg(savedItem.getId() + ", " + savedItem.getItem().getName() + " 티끌 수정에 성공했습니다.")
                .build();
    }

    @Transactional
    public ResponseMessage deleteSavedItem(Long savedItemId, User user) throws ItemException {
        SavedItem savedItem = itemValidator.isValidSavedItem(savedItemId, user);
        savedItemRepository.deleteById(savedItemId);
        return ResponseMessage.builder()
                .msg(savedItem.getId() + ", " + savedItem.getItem().getName() + " 티끌 삭제에 성공했습니다.")
                .build();
    }
}