package com.jaewon.novel_prizes_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder; // 추가

@MappedSuperclass
@Getter
@Setter
@SuperBuilder // 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위해 추가
// @AllArgsConstructor(access = AccessLevel.PROTECTED) // 필요시 추가
@EqualsAndHashCode(of = "id")
public abstract class BaseNovel {
    @Id
    protected String id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String genre;

    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NovelStatus status;

    private BigDecimal rating;
    private Integer viewCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}