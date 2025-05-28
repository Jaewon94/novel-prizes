package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelRepository extends JpaRepository<Novel, String> {
} 