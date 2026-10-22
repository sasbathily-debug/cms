package com.projet.site.controller;

import com.projet.site.dto.FaqItemDto;
import com.projet.site.dto.FaqItemRequest;
import com.projet.site.service.FaqItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/site/faq")
@RequiredArgsConstructor
public class FaqItemController {

    private final FaqItemService faqItemService;

    @GetMapping
    public List<FaqItemDto> findAll() {
        return faqItemService.findAll();
    }

    @GetMapping("/all")
    public List<FaqItemDto> findAllForAdmin() {
        return faqItemService.findAllForAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaqItemDto create(@Valid @RequestBody FaqItemRequest request) {
        return faqItemService.create(request);
    }

    @PutMapping("/{id}")
    public FaqItemDto update(@PathVariable UUID id, @Valid @RequestBody FaqItemRequest request) {
        return faqItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        faqItemService.delete(id);
    }
}
