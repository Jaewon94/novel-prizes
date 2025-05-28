package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, String> {
} 