package com.jaewon.novel_prizes_backend.repository;

import com.jaewon.novel_prizes_backend.entity.PlatformListing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformListingRepository extends JpaRepository<PlatformListing, String> {
} 