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
import lombok.AllArgsConstructor; // 추가
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// SuperBuilder import 제거 - 안정성을 위해 일반 Builder 사용

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위해 추가
@AllArgsConstructor(access = AccessLevel.PROTECTED) // 생성자 체인을 위해 추가
@EqualsAndHashCode(of = "id")
public abstract class BaseNovel {
    @Id
    protected String id; // 자식 클래스에서 @EqualsAndHashCode(of="id") 사용 시 protected 필요

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
