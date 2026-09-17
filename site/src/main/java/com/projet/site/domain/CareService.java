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
@Table(name = "care_services")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private String icon;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public CareService(String title, String description, String icon, int displayOrder) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.displayOrder = displayOrder;
    }
}
