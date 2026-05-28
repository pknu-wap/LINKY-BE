package com.project.linkybe_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "link",
        indexes = {
                @Index(name = "idx_link_device_uuid", columnList = "device_uuid"),
        }
)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    private String title;
    private String category;
    private Boolean isPrivate;
    private LocalDateTime selectedDate;
    private String isFavorite;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "device_uuid", nullable = false, length = 64)
    private String deviceUuid;

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}
