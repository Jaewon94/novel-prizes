package com.jaewon.novel_prizes_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor; // NoArgsConstructor와 함께 SuperBuilder 사용시 필요할 수 있음
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor; // SuperBuilder 사용 시 명시적 선언 권장
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder; // Builder -> SuperBuilder

@Entity
@Table(name = "free_novels")
@Getter
@Setter
@SuperBuilder // 변경
@NoArgsConstructor
@AllArgsConstructor // SuperBuilder와 함께 사용 시
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"novel", "author"})
public class FreeNovel extends BaseNovel {
    // BaseNovel에서 상속받는 필드들(id, title, ...)은 여기서 선언 X

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    private Novel novel; // NULL 허용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;
}