package com.unz.eval.notification;

import com.unz.eval.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataireOrderByCreatedAtDesc(User user);
    List<Notification> findByDestinataireAndLueFalseOrderByCreatedAtDesc(User user);
    long countByDestinataireAndLueFalse(User user);
}
