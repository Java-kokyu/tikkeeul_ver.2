package com.hanghae0705.sbmoney.service.item;

import com.hanghae0705.sbmoney.data.Message;
import com.hanghae0705.sbmoney.exception.Constants;
import com.hanghae0705.sbmoney.exception.ItemException;
import com.hanghae0705.sbmoney.model.domain.item.GoalItem;
import com.hanghae0705.sbmoney.model.domain.item.Item;
import com.hanghae0705.sbmoney.model.domain.item.SavedItem;
import com.hanghae0705.sbmoney.model.domain.user.Favorite;
import com.hanghae0705.sbmoney.model.domain.user.User;
import com.hanghae0705.sbmoney.repository.item.FavoriteRepository;
import com.hanghae0705.sbmoney.repository.item.GoalItemRepository;
import com.hanghae0705.sbmoney.service.S3Uploader;
import com.hanghae0705.sbmoney.util.MathFloor;
import com.hanghae0705.sbmoney.validator.ItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoalItemService {
    private final GoalItemRepository goalItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final S3Uploader s3Uploader;
    private final ItemValidator itemValidator;
    private final SavedItemService savedItemService;

    public Message getAllItems(User user) throws ItemException {
        GoalItem.Response goalItem = (GoalItem.Response) getGoalItem(user).getData();
        List<SavedItem.Response> savedItems = (List<SavedItem.Response>) savedItemService.getSavedItems(goalItem.getGoalItemId(), user).getData();
        GoalItem.AllResponse allResponse = new GoalItem.AllResponse(goalItem, savedItems);
        return new Message(true, "티끌 태산 조회에 성공하셨습니다.", allResponse);
    }

    public Message getGoalItem(User user) throws ItemException {
        List<GoalItem> goalItemList = user.getGoalItems();
        GoalItem.Response goalItemResponse = null;
        if (goalItemList.size() != 0) {
            for (GoalItem goalItem : goalItemList) {
                if (!goalItem.isCheckReached()) {
                    goalItemResponse = new GoalItem.Response(goalItem);
                }
            }
            if (goalItemResponse == null) { // 태산이 모두 달성했을 경우
                GoalItem noGoalItem = createNoGoalItem(user);
                goalItemResponse = new GoalItem.Response(goalItemRepository.save(noGoalItem));
            }
        } else { // 서비스를 처음 이용하는 사용자는 티끌도 태산도 존재하지 않으므로 목표 없음 생성
            GoalItem noGoalItem = createNoGoalItem(user);
            goalItemResponse = new GoalItem.Response(goalItemRepository.save(noGoalItem));
        }
        return new Message(true, "목표 항목을 조회하였습니다.", goalItemResponse);
    }

    public Message getHistory(User user) {
        List<GoalItem> goalItemList = user.getGoalItems();
        List<GoalItem.HistoryResponse> reachedGoalItemList = new ArrayList<>();
        for (GoalItem goalItem : goalItemList) {
            if (goalItem.isCheckReached()) {
                List<SavedItem> savedItemList = goalItem.getSavedItems();
                List<Favorite> favorites = favoriteRepository.findByUserId(user.getId());
                List<SavedItem.Response> savedItemResponseList = new ArrayList<>();
                int totalPrice = 0;
                for (SavedItem savedItem : savedItemList) {
                    Favorite.SavedItemResponse favorite = itemValidator.isFavoriteItem(favorites, savedItem.getItem(), savedItem.getPrice());
                    SavedItem.Response savedItemResponse = new SavedItem.Response(savedItem, favorite);
                    totalPrice += savedItem.getPrice();
                    savedItemResponseList.add(savedItemResponse);
                }
                Collections.reverse(savedItemResponseList);
                reachedGoalItemList.add(new GoalItem.HistoryResponse(goalItem, totalPrice, savedItemResponseList));
            }
        }
        return new Message(true, "히스토리를 성공적으로 조회하였습니다.", reachedGoalItemList);
    }

    @Transactional
    public Message postGoalItem(GoalItem.Request goalItemRequest, MultipartFile multipartFile, User user) throws ItemException, IOException {
        GoalItem goalItem = saveGoalItem(goalItemRequest, user);
        String url = s3Uploader.upload(multipartFile, "static");
        goalItem.setImage(url);

        return new Message(true, "목표 항목을 등록하였습니다.", goalItem);
    }

    @Transactional
    public Message postGoalItem(GoalItem.Request goalItemRequest, User user) throws ItemException {
        GoalItem goalItem = saveGoalItem(goalItemRequest, user);
        goalItem.setImage(goalItem.getItem().getCategory().getIconImg());

        return new Message(true, "목표 항목을 등록하였습니다.", goalItem);
    }

    @Transactional
    public GoalItem saveGoalItem(GoalItem.Request goalItemRequest, User user) throws ItemException {
        List<GoalItem> goalItemList = user.getGoalItems();
        if (goalItemList != null) {
            for (GoalItem goalItem : goalItemList) {
                if (!goalItem.isCheckReached() && goalItem.getItem().getId() != -1) {
                    throw new ItemException(Constants.ExceptionClass.GOAL_ITEM, HttpStatus.BAD_REQUEST, "이미 태산으로 등록된 상품이 존재합니다.");
                } else if (goalItem.getSavedItems().size() == 0) { // 티끌이 존재하지 않은 목표는 삭제
                    goalItemRepository.deleteById(goalItem.getId());
                } else { // 태산 없음으로 등록된 goalItem을 히스토리에 추가
                    LocalDateTime reachedDateTime = LocalDateTime.now();
                    goalItem.setCheckReached(true, reachedDateTime);
                }
            }
        }

        Long categoryId = goalItemRequest.getCategoryId();
        Long itemId = goalItemRequest.getItemId();
        Item item = itemValidator.isValidCategoryAndItem(categoryId, itemId);
        int count = goalItemRequest.getGoalItemCount();
        int price = goalItemRequest.getPrice();
        itemValidator.isValidNum(price);
        itemValidator.isValidNum(count);
        int total = (price == 0) ? item.getDefaultPrice() * count : goalItemRequest.getPrice() * count;
        return goalItemRepository.save(new GoalItem(user, count, total, item));
    }

    @Transactional
    public Message updateGoalItem(Long goalItemId, GoalItem.Request goalItemRequest, MultipartFile multipartFile, User user) throws ItemException, IOException {
        GoalItem goalItem = changeGoalItem(goalItemId, goalItemRequest, user);
        String url = s3Uploader.upload(multipartFile, "static");
        goalItem.setImage(url);
        return new Message(true, "목표 항목을 수정하였습니다.", new GoalItem.Response(goalItem));
    }

    @Transactional
    public Message updateGoalItem(Long goalItemId, GoalItem.Request goalItemRequest, User user) throws ItemException, IOException {
        GoalItem goalItem = changeGoalItem(goalItemId, goalItemRequest, user);
        goalItem.setImage(goalItem.getItem().getCategory().getIconImg());
        return new Message(true, "목표 항목을 수정하였습니다.", new GoalItem.Response(goalItem));
    }

    @Transactional
    public GoalItem changeGoalItem(Long goalItemId, GoalItem.Request goalItemRequest, User user) throws ItemException, IOException {
        GoalItem goalItem = itemValidator.isValidGoalItem(goalItemId, user);

        // 목표 품목을 변경할 때
        if (goalItemRequest.getItemId() != -1) {
            Long categoryId = goalItemRequest.getCategoryId();
            Long itemId = goalItemRequest.getItemId();
            Item item = itemValidator.isValidCategoryAndItem(categoryId, itemId);
            int count = goalItemRequest.getGoalItemCount();
            int price = goalItemRequest.getPrice();
            itemValidator.isValidNum(price);
            itemValidator.isValidNum(count);
            int total = (price == 0) ? item.getDefaultPrice() * count : goalItemRequest.getPrice() * count;
            int savedItemTotal = 0;
            double goalPercent = 1.0;

            List<SavedItem> savedItems = goalItem.getSavedItems();

            for (SavedItem savedItem : savedItems) {
                savedItemTotal += savedItem.getPrice();
            }

            double decimal = (double) savedItemTotal / total;
            goalPercent = MathFloor.PercentTenths(decimal);

            if (savedItemTotal >= total) { // 변경한 품목이 달성률 100%를 넘은 지점
                LocalDateTime reachedAt = LocalDateTime.now();
                goalItem.setCheckReached(true, reachedAt);
            }
            goalItem.updateGoalItem(count, total, item, goalPercent);
        }
        // 목표 품목을 변경하지 않을 때
        else {
            int itemDefaultPrice = goalItem.getItem().getDefaultPrice();
            int count = goalItemRequest.getGoalItemCount();
            int price = goalItemRequest.getPrice();
            itemValidator.isValidNum(price);
            itemValidator.isValidNum(count);
            int total = (price == 0) ? itemDefaultPrice * count : goalItemRequest.getPrice() * count;
            int savedItemTotal = 0;

            List<SavedItem> savedItems = goalItem.getSavedItems();
            for (SavedItem savedItem : savedItems) {
                savedItemTotal += savedItem.getPrice();
                if (savedItemTotal >= total) { // 수량/금액 변경으로 달성률 100%를 넘은 지점
                    LocalDateTime reachedAt = LocalDateTime.now();
                    goalItem.setCheckReached(true, reachedAt);
                }
            }
            double decimal = (double) savedItemTotal / total;
            double goalPercent = MathFloor.PercentTenths(decimal);
            goalItem.updateGoalItem(count, total, goalPercent);
        }
        return goalItem;
    }

    @Transactional
    public Message deleteGoalItem(Long goalItemId, User user) throws ItemException {
        GoalItem goalItem = itemValidator.isValidGoalItem(goalItemId, user);
        List<SavedItem> savedItemList = goalItem.getSavedItems();
        //목표 없음 생성 후 히스토리에 저장
        GoalItem noGoalItem = createNoGoalItem(user);
        for (SavedItem savedItem : savedItemList) {
            savedItem.setGoalItem(noGoalItem);
        }
        goalItemRepository.deleteById(goalItemId);
        goalItemRepository.save(noGoalItem);
        LocalDateTime nowDate = LocalDateTime.now();
        noGoalItem.setCheckReached(true, 100.0, nowDate);
        return new Message(true, "목표 항목을 삭제하였습니다.");
    }

    @Transactional
    public Message checkReached(User user, Long goalItemId) throws ItemException {
        GoalItem goalItem = itemValidator.isValidGoalItem(goalItemId, user);
        LocalDateTime reachedAt = LocalDateTime.now();
        goalItem.setCheckReached(true, reachedAt);
        createNoGoalItem(user);

        return new Message(true, "목표 항목을 달성하였습니다.");
    }

    public GoalItem createNoGoalItem(User user) throws ItemException {
        Item item = itemValidator.isValidItem(-1L); // 목표 없음 카테고리

        return new GoalItem(user, 0, 0, item);
    }


}