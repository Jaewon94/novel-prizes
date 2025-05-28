package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
} 