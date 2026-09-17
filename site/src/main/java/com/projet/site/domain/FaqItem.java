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
@Table(name = "faq_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FaqItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public FaqItem(String question, String answer, int displayOrder) {
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
    }
}
