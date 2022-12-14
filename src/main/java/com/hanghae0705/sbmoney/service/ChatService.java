package com.hanghae0705.sbmoney.service;


import com.hanghae0705.sbmoney.data.Message;
import com.hanghae0705.sbmoney.data.MessageChat;
import com.hanghae0705.sbmoney.model.domain.chat.ChatRoomProsCons;
import com.hanghae0705.sbmoney.model.domain.chat.RedisChatRoom;
import com.hanghae0705.sbmoney.model.domain.chat.entity.ChatLog;
import com.hanghae0705.sbmoney.model.domain.chat.ChatMessage;
import com.hanghae0705.sbmoney.model.domain.chat.entity.ChatRoom;
import com.hanghae0705.sbmoney.model.domain.user.User;
import com.hanghae0705.sbmoney.repository.chat.ChatLogRepository;
import com.hanghae0705.sbmoney.repository.chat.ChatRoomProsConsRepository;
import com.hanghae0705.sbmoney.repository.chat.ChatRoomRepository;
import com.hanghae0705.sbmoney.repository.chat.RedisChatRoomRepository;
import com.hanghae0705.sbmoney.validator.ChatRoomValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private static final int DEFAULT_PAGE_NUM = 0;
    private final ChannelTopic channelTopic;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, ChatMessage> redisChatMessageTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomProsConsRepository chatRoomProsConsRepository;
    private final RedisChatRoomRepository redisChatRoomRepository;
    private final ChatLogRepository chatLogRepository;
    private final CommonService commonService;
    private final ChatRoomValidator chatRoomValidator;

    /**
     * destination???????????? roomId ??????
     */
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

    /**
     * ???????????? ????????? ??????
     */
    public void sendChatMessage(ChatMessage chatMessage) {
        chatMessage.setUserCount(redisChatRoomRepository.getUserCount(chatMessage.getRoomId()));
        chatMessage.setTimeLimit(redisChatRoomRepository
                .findRoomById(chatMessage.getRoomId()).getTimeLimit());

        // ??????????????? ?????? ????????? ????????? ????????????.
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatMessage.getRoomId()).orElseThrow(() -> new IllegalArgumentException("???????????? ?????? ????????????."));
        long leftTime = getLeftTime(chatRoom);
        chatMessage.setLeftTime(leftTime);

        log.info("CHAT {}, {}", redisChatRoomRepository.findRoomById(chatMessage.getRoomId()), leftTime);

        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "?????? ?????? ??????????????????.");
            chatMessage.setSender("[??????]");
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "?????? ????????? ???????????????.");
            chatMessage.setSender("[??????]");
        }

        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }

    // ?????? ?????? ?????? ??????
    public Message getRooms() {
        Long userId = commonService.getUserId();
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByProceedingOrderByCreatedAtDesc(true);
        List<ChatRoom.Response> chatRoomResponseList = new ArrayList<>();
        //proceeding(true/false)
        for (ChatRoom chatRoom : chatRooms) {
            Long userCount = redisChatRoomRepository.getUserCount(chatRoom.getRoomId());
            List<ChatRoomProsCons> chatRoomProsConsList = chatRoom.getChatRoomProsConsList();
            int checkProsCons = 0;
            //?????? ????????? ????????? ??? ??????
            if (!chatRoomProsConsList.isEmpty()) {
                for (ChatRoomProsCons chatRoomProsCons : chatRoomProsConsList) {
                    if (chatRoomProsCons.getUserId().equals(userId)) {
                        checkProsCons = chatRoomProsCons.getProsCons();
                        break;
                    }
                }
            }

            // ?????? ?????? ???????????? ??????
            long leftTime = getLeftTime(chatRoom);

            chatRoomResponseList.add(new ChatRoom.Response(chatRoom, checkProsCons, userCount, leftTime));
        }
        return Message.builder()
                .result(true)
                .respMsg("????????? ?????? ????????? ??????????????????.")
                .data(chatRoomResponseList)
                .build();
    }

    // ????????? ?????? ??????
    public Message getRoomDetail(String roomId) {
        ChatRoom chatRoom = chatRoomValidator.isValidChatRoom(roomId);
        Long userCount = redisChatRoomRepository.getUserCount(roomId);
        return Message.builder()
                .result(true)
                .respMsg("????????? ?????? ????????? ??????????????????.")
                .data(new ChatRoom.Response(chatRoom, userCount, getLeftTime(chatRoom)))
                .build();
    }

    // ????????? ??????
    public Message createRoom(ChatRoom.Request request) {
        User user = commonService.getUser();
        String RoomUuid = UUID.randomUUID().toString();
        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(user, request.getTimeLimit(), request.getComment(), RoomUuid, true));
        String redisChatRoomId = chatRoom.getRoomId();
        redisChatRoomRepository.createChatRoom(redisChatRoomId, request.getComment(), request.getTimeLimit());
        return Message.builder()
                .result(true)
                .respMsg("??? ????????? ?????????????????????.")
                .data(new ChatRoom.Response(chatRoom, 0L))
                .build();
    }

    // ??????
    public Message vote(String roomId, ChatRoomProsCons.Request chatRoomProsConsRequest) {
        Long userId = commonService.getUserId();
        ChatRoom chatRoom = chatRoomValidator.isValidChatRoom(roomId);
        int prosCons = chatRoomProsConsRequest.getProsCons();
        ChatRoomProsCons checkVote = chatRoomProsConsRepository.findByUserIdAndChatRoom(userId, chatRoom);
        if (checkVote != null) {
            if (prosCons != checkVote.getProsCons()) {
                chatRoom.MinusVoteCount((prosCons == 1) ? 2 : 1);
                chatRoom.PlusVoteCount(prosCons);
                checkVote.update(chatRoomProsConsRequest.getProsCons());
            }
        } else {
            ChatRoomProsCons chatRoomProsCons = new ChatRoomProsCons(chatRoomProsConsRequest.getProsCons(), userId, chatRoom);
            chatRoomProsConsRepository.save(chatRoomProsCons);
            chatRoom.PlusVoteCount(prosCons);
        }
        return Message.builder()
                .result(true)
                .respMsg("??????? ??????? ????????? ???????????????.")
                .data(new ChatRoom.VoteResponse(chatRoom))
                .build();
    }

    /**
     * ?????? ?????? ??? ?????? ?????? ??????
     */
    @Transactional
    public Message saveChatLog(String roomId) {
        //redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatMessage.class));
        RedisOperations<String, ChatMessage> operations = redisChatMessageTemplate.opsForList().getOperations();

        ChatRoom chatRoom = chatRoomValidator.isValidChatRoom(roomId);

        // proceeding ?????????(false)?????? ??????
        chatRoom.changeProceeding(false);

        // roomId??? ???????????? ChatMessage??? ????????? ChatLog??? ??????
        List<ChatMessage> chatMessageList = operations.opsForList().range(roomId, 0, -1);

        for (ChatMessage chatMessage : chatMessageList) {
            ChatLog chatLog = ChatLog.builder()
                    .id(null)
                    .type(chatMessage.getType())
                    .profileImg(chatMessage.getProfileImg())
                    .nickname(chatMessage.getSender())
                    .message(chatMessage.getMessage())
                    .chatRoom(chatRoom)
                    .build();
            chatLogRepository.save(chatLog);
        }

        return Message.builder()
                .result(true)
                .respMsg("?????? ?????? ????????? ??????????????????.")
                .data(chatMessageList)
                .build();
    }

    // userCount ?????? ?????? 5??? ??????
    public Message getTopRoom() {
        Long userId = commonService.getUserId();

        // ?????? roomId ??????
        List<RedisChatRoom> allRooms = redisChatRoomRepository.findAllRoom();
        for (RedisChatRoom room : allRooms) {
            Long userCount = redisChatRoomRepository.getUserCount(room.getRoomId());
            room.setUserCount(userCount);
        }

        // roomId??? ???????????? userCount ??????
        Map<String, Long> roomMap = allRooms.stream()
                .collect(Collectors.toMap(
                        RedisChatRoom::getRoomId,
                        RedisChatRoom::getUserCount
                ));

        // userCount ????????? ????????????
        Map<String, Long> topRoom = roomMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                //.limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new)
                );

        int checkProsCons = 0;
        // topRoom ???, proceeding??? true??? ?????? DB?????? chatRoom ????????? ????????????
        List<ChatRoom.Response> chatRoomList = topRoom.keySet().stream()
                .map(chatRoomId -> chatRoomRepository.findByRoomIdAndProceeding(chatRoomId, true))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(room ->
                        ChatRoom.Response.builder()
                                .chatRoom(room)
                                .userCount(topRoom.get(room.getRoomId()))
                                .chatRoomProsCons(getCheckProsCons(userId, checkProsCons, room.getChatRoomProsConsList()))
                                .leftTime(
                                        (room.getTimeLimit() * 60L)
                                                - Duration.between(room.getCreatedDate(), LocalDateTime.now())
                                                .getSeconds() < 0 ? 0L : (room.getTimeLimit() * 60L)
                                                - Duration.between(room.getCreatedDate(), LocalDateTime.now())
                                                .getSeconds())
                                .build()
                )
                .collect(Collectors.toList());

        return Message.builder()
                .result(true)
                .respMsg("?????? 5??? ????????? ?????????????????????.")
                .data(chatRoomList)
                .build();
    }

    // ?????? ?????? ?????? ??????
    public Message getClosedRooms() {
        List<ChatRoom> closedChatRooms = chatRoomRepository.findAllByProceedingOrderByCreatedAtDesc(false);
        List<ChatRoom.ClosedResponse> chatRoomResponseList = new ArrayList<>();
        Message message;

        if (closedChatRooms.isEmpty()) {
            message = Message.builder()
                    .result(false)
                    .respMsg("????????? ???????????? ????????????.")
                    .build();
        } else {
            for (ChatRoom chatRoom : closedChatRooms) {
                if (chatRoom.getUser() == null) {
                    chatRoom.changeUser(User.builder()
                            .nickname("????????????")
                            .profileImg("none")
                            .build());
                }
                chatRoomResponseList.add(new ChatRoom.ClosedResponse(chatRoom));
            }

            message = Message.builder()
                    .result(true)
                    .respMsg("????????? ?????? ?????? ????????? ??????????????????.")
                    .data(chatRoomResponseList)
                    .build();
        }
        return message;
    }

    // ?????? ?????? ?????? ??????
    public Message getCloesdChatRoom(String closedRoomId) {
        // ?????? ??????(?????????, ????????? ??????, ?????????, ???/??? ??????, ?????????) ????????????
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(closedRoomId).orElseThrow(
                () -> new IllegalArgumentException("???????????? ?????? ????????????.")
        );

        float totalCount = chatRoom.getVoteTrueCount() + chatRoom.getVoteFalseCount();
        int voteTruePercent = Math.round(chatRoom.getVoteTrueCount() / totalCount * 100);
        int voteFalsePercent = Math.round(chatRoom.getVoteFalseCount() / totalCount * 100);

        ChatRoom.ClosedRoomDetail closedRoomDetail = ChatRoom.ClosedRoomDetail.builder()
                .closedRoomId(chatRoom.getRoomId())
                .authorNickname(chatRoom.getUser().getNickname())
                .authorProfileImg(chatRoom.getUser().getProfileImg())
                .comment(chatRoom.getComment())
                .voteTruePercent(voteTruePercent)
                .voteFalsePercent(voteFalsePercent)
                .chatLogList(chatRoom.getChatLogList())
                .build();

        return Message.builder()
                .result(true)
                .respMsg("????????? ?????? ????????? ??????????????????.")
                .data(closedRoomDetail)
                .build();
    }

    public MessageChat getAllList() {
        Long userId = commonService.getUserId();
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByOrderByCreatedAtDesc();
        List<ChatRoom.Response> openChatRoomList = new ArrayList<>();
        List<ChatRoom.ClosedResponse> closedChatRoomList = new ArrayList<>();
        List<ChatRoom.Response> topRoomList = new ArrayList<>();

        //????????? ??????
        for (ChatRoom chatRoom : chatRoomList) {
            // ?????? ?????? ???????????? ??????
            long leftTime = getLeftTime(chatRoom);
            Long userCount = redisChatRoomRepository.getUserCount(chatRoom.getRoomId());
            List<ChatRoomProsCons> chatRoomProsConsList = chatRoom.getChatRoomProsConsList();
            int checkProsCons = 0;

            if (chatRoom.getProceeding()) {
                checkProsCons = getCheckProsCons(userId, checkProsCons, chatRoomProsConsList);
                openChatRoomList.add(new ChatRoom.Response(chatRoom, checkProsCons, userCount, leftTime));
                topRoomList.add(new ChatRoom.Response(chatRoom, checkProsCons, userCount, leftTime));
            } else {
                closedChatRoomList.add(new ChatRoom.ClosedResponse(chatRoom));
            }

            if (chatRoom.getUser() == null) {
                chatRoom.changeUser(User.builder()
                        .nickname("????????????")
                        .profileImg("https://s3.ap-northeast-2.amazonaws.com/tikkeeul.com/KakaoTalk_Image_2022-07-29-17-35-29.png")
                        .build());
            }
        }

        //top5 ?????????
        List<ChatRoom.Response> top5RoomList = topRoomList.stream()
                .sorted(Comparator.comparing(ChatRoom.Response::getUserCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return MessageChat.builder()
                .result(true)
                .respMsg("top5, ????????? ?????????, ?????? ????????? ????????? ??????????????????.")
                .top5(top5RoomList)
                .chatRooms(openChatRoomList)
                .closedChatRooms(closedChatRoomList)
                .build();
    }

    public MessageChat getAlChatRoom(Long lastChatRoomId, int size) {
            Long userId = commonService.getUserId();
            PageRequest pageRequest = PageRequest.of(DEFAULT_PAGE_NUM, size);
            Page<ChatRoom> chatRoomList = chatRoomRepository.findByIdLessThanOrderByIdDesc(lastChatRoomId, pageRequest);
            List<ChatRoom.Response> openChatRoomList = new ArrayList<>();
            List<ChatRoom.ClosedResponse> closedChatRoomList = new ArrayList<>();
            List<ChatRoom.Response> topRoomList = new ArrayList<>();

        //????????? ??????
        for (ChatRoom chatRoom : chatRoomList) {
            // ?????? ?????? ???????????? ??????
            long leftTime = getLeftTime(chatRoom);
            Long userCount = redisChatRoomRepository.getUserCount(chatRoom.getRoomId());
            List<ChatRoomProsCons> chatRoomProsConsList = chatRoom.getChatRoomProsConsList();
            int checkProsCons = 0;

            if (chatRoom.getProceeding()) {
                checkProsCons = getCheckProsCons(userId, checkProsCons, chatRoomProsConsList);
                openChatRoomList.add(new ChatRoom.Response(chatRoom, checkProsCons, userCount, leftTime));
                topRoomList.add(new ChatRoom.Response(chatRoom, checkProsCons, userCount, leftTime));
            } else {
                closedChatRoomList.add(new ChatRoom.ClosedResponse(chatRoom));
            }

            if (chatRoom.getUser() == null) {
                chatRoom.changeUser(User.builder()
                        .nickname("????????????")
                        .profileImg("https://s3.ap-northeast-2.amazonaws.com/tikkeeul.com/KakaoTalk_Image_2022-07-29-17-35-29.png")
                        .build());
            }
        }

        //top5 ?????????
        List<ChatRoom.Response> top5RoomList = topRoomList.stream()
                .sorted(Comparator.comparing(ChatRoom.Response::getUserCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return MessageChat.builder()
                .result(true)
                .respMsg("top5, ????????? ?????????, ?????? ????????? ????????? ??????????????????.")
                .top5(top5RoomList)
                .chatRooms(openChatRoomList)
                .closedChatRooms(closedChatRoomList)
                .build();
    }

    private long getLeftTime(ChatRoom chatRoom) {
        long betweenSeconds = Duration.between(chatRoom.getCreatedDate(), LocalDateTime.now()).getSeconds();
        return ((chatRoom.getTimeLimit() * 60L) - betweenSeconds) < 0 ? 0L : ((chatRoom.getTimeLimit() * 60L) - betweenSeconds);
    }

    //?????? ????????? ????????? ??? ??????
    private int getCheckProsCons(Long userId, int checkProsCons, List<ChatRoomProsCons> chatRoomProsConsList) {
        if (!chatRoomProsConsList.isEmpty()) {
            for (ChatRoomProsCons chatRoomProsCons : chatRoomProsConsList) {
                if (chatRoomProsCons.getUserId().equals(userId)) {
                    checkProsCons = chatRoomProsCons.getProsCons();
                    break;
                }
            }
        }
        return checkProsCons;
    }
}
