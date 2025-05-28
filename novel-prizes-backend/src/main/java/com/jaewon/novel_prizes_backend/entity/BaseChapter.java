package com.jaewon.novel_prizes_backend.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel; // 추가
import lombok.Builder; // SuperBuilder.Default 사용을 위해 유지
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor; // 추가
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위해 추가
// @AllArgsConstructor(access = AccessLevel.PROTECTED) // 필요시 추가
@EqualsAndHashCode(of = "id")
public abstract class BaseChapter {
    @Id
    protected String id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default // SuperBuilder 사용시 @SuperBuilder.Default로 변경하거나, Builder.Default도 호환됨
    private Integer chapterNumber = 1;

    private Integer viewCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}