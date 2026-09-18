package com.projet.site.controller;

import com.projet.site.dto.AvailabilityRuleDto;
import com.projet.site.dto.AvailabilityRuleRequest;
import com.projet.site.dto.AvailableSlotDto;
import com.projet.site.dto.ScheduleExceptionDto;
import com.projet.site.dto.ScheduleExceptionRequest;
import com.projet.site.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/site/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public List<AvailableSlotDto> computeSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return availabilityService.computeAvailableSlots(from, to);
    }

    @GetMapping("/rules")
    public List<AvailabilityRuleDto> findAllRules() {
        return availabilityService.findAllRules();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityRuleDto createRule(@Valid @RequestBody AvailabilityRuleRequest request) {
        return availabilityService.createRule(request);
    }

    @PutMapping("/rules/{id}")
    public AvailabilityRuleDto updateRule(@PathVariable UUID id, @Valid @RequestBody AvailabilityRuleRequest request) {
        return availabilityService.updateRule(id, request);
    }

    @DeleteMapping("/rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable UUID id) {
        availabilityService.deleteRule(id);
    }

    @GetMapping("/exceptions")
    public List<ScheduleExceptionDto> findAllExceptions() {
        return availabilityService.findAllExceptions();
    }

    @PostMapping("/exceptions")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleExceptionDto createException(@Valid @RequestBody ScheduleExceptionRequest request) {
        return availabilityService.createException(request);
    }

    @PutMapping("/exceptions/{id}")
    public ScheduleExceptionDto updateException(@PathVariable UUID id, @Valid @RequestBody ScheduleExceptionRequest request) {
        return availabilityService.updateException(id, request);
    }

    @DeleteMapping("/exceptions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteException(@PathVariable UUID id) {
        availabilityService.deleteException(id);
    }
}
