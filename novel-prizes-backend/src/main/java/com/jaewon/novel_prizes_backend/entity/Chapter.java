package com.jaewon.novel_prizes_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder; // SuperBuilder.Default 사용을 위해 유지
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder; // Builder -> SuperBuilder

@Entity
@Table(name = "chapters")
@Getter
@Setter
@SuperBuilder // 변경
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"novel"})
public class Chapter extends BaseChapter {
    // BaseChapter에서 상속받는 필드들(id, title, content, chapterNumber, viewCount, createdAt, updatedAt)은 여기서 선언 X

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false)
    @Builder.Default // 또는 @SuperBuilder.Default
    private Boolean isFree = false;

    @Column(nullable = false)
    @Builder.Default // 또는 @SuperBuilder.Default
    private Integer price = 0;
}