package com.jaewon.novel_prizes_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor; // 추가
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor; // 추가
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "free_chapters")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor // 추가
@AllArgsConstructor // 추가
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"freeNovel"})
public class FreeChapter extends BaseChapter {
    // BaseChapter에서 상속받는 필드들(id, title, content, chapterNumber, viewCount, createdAt, updatedAt)은 여기서 선언 X

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "free_novel_id", nullable = false)
    private FreeNovel freeNovel;
}