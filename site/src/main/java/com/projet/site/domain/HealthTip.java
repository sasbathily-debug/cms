package com.projet.site.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "health_tips")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthTip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    private String icon;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private String category;

    @Column(name = "reading_minutes")
    private Integer readingMinutes;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(nullable = false)
    private boolean published = true;

    public HealthTip(String title, String body, String icon, int displayOrder) {
        this.title = title;
        this.body = body;
        this.icon = icon;
        this.displayOrder = displayOrder;
    }
}
