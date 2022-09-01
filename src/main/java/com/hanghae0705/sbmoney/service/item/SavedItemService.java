package com.hanghae0705.sbmoney.service.item;

import com.hanghae0705.sbmoney.data.MessageWithData;
import com.hanghae0705.sbmoney.data.MessageWithNoData;
import com.hanghae0705.sbmoney.exception.ItemException;
import com.hanghae0705.sbmoney.model.domain.item.Item;
import com.hanghae0705.sbmoney.model.domain.item.SavedItem;
import com.hanghae0705.sbmoney.model.domain.user.User;
import com.hanghae0705.sbmoney.repository.item.SavedItemRepository;
import com.hanghae0705.sbmoney.validator.ItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public MessageWithNoData postSavedItem(SavedItem.Request savedItemRequest, User user) throws ItemException {
        Item item = itemValidator.isValidItem(savedItemRequest.getItemId());
        int price = (savedItemRequest.getPrice() == 0) ? item.getDefaultPrice() : savedItemRequest.getPrice();
        itemValidator.isValidPrice(savedItemRequest.getPrice());

        savedItemRepository.save(new SavedItem(item, price, user));

        return MessageWithNoData.builder()
                .msg("티끌 등록에 성공하였습니다.")
                .build();
    }

    public MessageWithData getSavedItems(User user) {
        List<SavedItem> savedItemList = user.getSavedItems();
        int totalPrice = getTotalPrice(user);
        List<SavedItem.Response> savedItemResponseList = new ArrayList<>();
        for (SavedItem savedItem : savedItemList) {
            SavedItem.Response savedItemResponse = new SavedItem.Response(savedItem);
            savedItemResponseList.add(savedItemResponse);
        }
        Collections.reverse(savedItemResponseList); //id 내림차순 정렬
        SavedItem.IntegrateResponse response = new SavedItem.IntegrateResponse(totalPrice, savedItemResponseList);
        return MessageWithData.builder()
                .msg("티끌 조회에 성공하였습니다.")
                .data(response)
                .build();
    }

    @Transactional
    public MessageWithNoData updateSavedItem(Long savedItemId, SavedItem.Update price, User user) throws ItemException {
        SavedItem savedItem = itemValidator.isValidSavedItem(savedItemId, user);
        itemValidator.isValidPrice(price.getPrice());
        savedItem.update(price.getPrice());

        return MessageWithNoData.builder()
                .msg(savedItem.getId() + ", " + savedItem.getItem().getName() + " 티끌 수정에 성공했습니다.")
                .build();
    }

    @Transactional
    public MessageWithNoData deleteSavedItem(Long savedItemId, User user) throws ItemException {
        SavedItem savedItem = itemValidator.isValidSavedItem(savedItemId, user);
        savedItemRepository.deleteById(savedItemId);
        return MessageWithNoData.builder()
                .msg(savedItem.getId() + ", " + savedItem.getItem().getName() + " 티끌 삭제에 성공했습니다.")
                .build();
    }
}