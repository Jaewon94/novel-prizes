package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, String> {
} 