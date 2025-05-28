package com.jaewon.novel_prizes_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@EqualsAndHashCode(callSuper = true) // 부모 클래스의 id 기준 equals/hashCode 사용
@ToString(exclude = {"novel"})
public class Chapter extends BaseChapter {
    // BaseChapter에서 상속받는 필드들은 여기서 다시 선언하지 않습니다.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isFree = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer price = 0;
}
