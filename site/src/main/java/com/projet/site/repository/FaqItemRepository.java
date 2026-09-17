package com.projet.site.repository;

import com.projet.site.domain.FaqItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FaqItemRepository extends JpaRepository<FaqItem, UUID> {

    List<FaqItem> findAllByOrderByDisplayOrderAsc();
}
