package com.hanghae0705.sbmoney.repository.chat;

import com.hanghae0705.sbmoney.model.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findById(Long id);
    Optional<ChatRoom> findByRoomId(String roomUuid);
    Optional<ChatRoom> findByRoomIdAndProceeding(String roomUuid, Boolean proceeding);
    List<ChatRoom> findAllByOrderByCreatedAtDesc();
    List<ChatRoom> findAllByProceedingOrderByCreatedAtDesc(Boolean proceeding);
    List<ChatRoom> findAllByUserId(Long userId);
    Page<ChatRoom> findByIdLessThanOrderByIdDesc(Long lastBoardId, Pageable pageable);
}
