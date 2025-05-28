package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.NovelStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelStatisticsRepository extends JpaRepository<NovelStatistics, String> {
} 