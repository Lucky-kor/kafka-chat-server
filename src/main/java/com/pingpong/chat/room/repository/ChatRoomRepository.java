package com.pingpong.chat.room.repository;

import com.pingpong.chat.room.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.util.List;

@EnableJpaRepositories
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    ChatRoom findByChatRoomId(String chatRoomId);
//    List<ChatRoom> findByParticipantsUserId(String userId);
@Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.userId = :userId")
List<ChatRoom> findByParticipantsUserId(@Param("userId") String userId);
}
