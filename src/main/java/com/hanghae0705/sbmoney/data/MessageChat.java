package com.hanghae0705.sbmoney.data;

import lombok.Builder;
import lombok.Data;

@Data
public class MessageChat {

    private boolean result;
    private String respMsg;
    private Object top5;
    private Object chatRooms;
    private Object closedChatRooms;

    @Builder
    public MessageChat(boolean result, String respMsg, Object top5, Object chatRooms, Object closedChatRooms) {
        this.result = result;
        this.respMsg = respMsg;
        this.top5 = top5;
        this.chatRooms = chatRooms;
        this.closedChatRooms = closedChatRooms;
    }
}


