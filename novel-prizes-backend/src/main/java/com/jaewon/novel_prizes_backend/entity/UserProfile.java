package com.jaewon.novel_prizes_backend.entity;

import java.time.LocalDateTime;
import java.util.Map;
import com.jaewon.novel_prizes_backend.converter.JsonMapConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "userId")
@ToString(exclude = {"user"})
public class UserProfile {
    @Id
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private String profileImageUrl;
    private String websiteUrl;

    @Column(columnDefinition = "json")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> socialLinks;

    @Column(columnDefinition = "json")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> preferences;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
} 