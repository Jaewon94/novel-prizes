package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.FreeNovel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeNovelRepository extends JpaRepository<FreeNovel, String> {
} 