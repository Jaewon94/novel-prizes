package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.CategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryMappingRepository extends JpaRepository<CategoryMapping, String> {
} 