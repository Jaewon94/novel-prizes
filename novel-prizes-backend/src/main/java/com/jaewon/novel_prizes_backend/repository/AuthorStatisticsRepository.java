package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.AuthorStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorStatisticsRepository extends JpaRepository<AuthorStatistics, String> {
} 