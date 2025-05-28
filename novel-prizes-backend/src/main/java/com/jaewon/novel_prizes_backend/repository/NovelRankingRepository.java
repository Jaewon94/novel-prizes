package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.NovelRanking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelRankingRepository extends JpaRepository<NovelRanking, String> {
} 