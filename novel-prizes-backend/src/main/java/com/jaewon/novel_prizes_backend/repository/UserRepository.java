package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
} 