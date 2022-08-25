package com.hanghae0705.sbmoney.model.domain.chat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hanghae0705.sbmoney.model.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@RequiredArgsConstructor
public class ChatRoomProsCons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @ColumnDefault("0")
    private int prosCons;

    @Column
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "CHATROOM_ID")
    @JsonBackReference(value = "chatRoom-fk")
    private ChatRoom chatRoom;

    public ChatRoomProsCons(int prosCons, Long userId, ChatRoom chatRoom){
        this.prosCons = prosCons;
        this.userId = userId;
        this.chatRoom = chatRoom;
    }

    public int update(int prosCons){
        this.prosCons = prosCons;
        return prosCons;
    }

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        int prosCons;
    }

}
