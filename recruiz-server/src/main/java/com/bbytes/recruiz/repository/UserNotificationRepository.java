package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

}
