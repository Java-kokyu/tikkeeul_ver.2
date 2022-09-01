package com.hanghae0705.sbmoney.controller.item;

import com.hanghae0705.sbmoney.data.MessageWithData;
import com.hanghae0705.sbmoney.data.MessageWithNoData;
import com.hanghae0705.sbmoney.exception.ItemException;
import com.hanghae0705.sbmoney.model.domain.item.SavedItem;
import com.hanghae0705.sbmoney.model.domain.user.User;
import com.hanghae0705.sbmoney.service.CommonService;
import com.hanghae0705.sbmoney.service.item.SavedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SavedItemController {
    private final SavedItemService savedItemService;
    private final CommonService commonService;

    @GetMapping("/api/savedItem")
    private ResponseEntity<MessageWithData> getSavedItem() {
        User user = commonService.getUser();
        MessageWithData message = savedItemService.getSavedItems(user);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/api/savedItem")
    private ResponseEntity<MessageWithNoData> postSavedItem(@RequestBody SavedItem.Request savedItemRequest) throws ItemException {
        User user = commonService.getUser();
        MessageWithNoData message = savedItemService.postSavedItem(savedItemRequest, user);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/api/savedItem/{savedItemId}")
    private ResponseEntity<MessageWithNoData> updateSavedItem(@PathVariable Long savedItemId, @RequestBody SavedItem.Update price) throws ItemException {
        User user = commonService.getUser();
        MessageWithNoData message = savedItemService.updateSavedItem(savedItemId, price, user);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/api/savedItem/{savedItemId}")
    private ResponseEntity<MessageWithNoData> deleteSavedItem(@PathVariable Long savedItemId) throws ItemException {
        User user = commonService.getUser();
        MessageWithNoData message = savedItemService.deleteSavedItem(savedItemId, user);
        return ResponseEntity.ok(message);
    }
}