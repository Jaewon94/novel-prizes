package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, String> {
} 