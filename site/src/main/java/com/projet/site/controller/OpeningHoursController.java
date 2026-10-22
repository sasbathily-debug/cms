package com.projet.site.controller;

import com.projet.site.dto.OpeningHourDto;
import com.projet.site.dto.UpdateOpeningHoursRequest;
import com.projet.site.service.OpeningHoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/site/opening-hours")
@RequiredArgsConstructor
public class OpeningHoursController {

    private final OpeningHoursService openingHoursService;

    @GetMapping
    public List<OpeningHourDto> findAll() {
        return openingHoursService.findAll();
    }

    @PutMapping
    public List<OpeningHourDto> update(@Valid @RequestBody UpdateOpeningHoursRequest request) {
        return openingHoursService.update(request);
    }
}
