package com.jaewon.novel_prizes_backend.entity;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

@Entity
@Table(name = "novels")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // 부모 클래스의 id 기준 equals/hashCode 사용
@ToString(exclude = {"author", "chapters", "platformListings"})
public class Novel extends BaseNovel {
    // BaseNovel에서 상속받는 필드들은 여기서 다시 선언하지 않습니다.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String platformUrl;

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<Chapter> chapters;

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<PlatformListing> platformListings;
}
