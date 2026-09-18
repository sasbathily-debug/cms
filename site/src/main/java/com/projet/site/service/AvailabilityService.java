package com.projet.site.service;

import com.projet.site.dto.AvailabilityRuleDto;
import com.projet.site.dto.AvailabilityRuleRequest;
import com.projet.site.dto.AvailableSlotDto;
import com.projet.site.dto.ScheduleExceptionDto;
import com.projet.site.dto.ScheduleExceptionRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {

    List<AvailabilityRuleDto> findAllRules();

    AvailabilityRuleDto createRule(AvailabilityRuleRequest request);

    AvailabilityRuleDto updateRule(UUID id, AvailabilityRuleRequest request);

    void deleteRule(UUID id);

    List<ScheduleExceptionDto> findAllExceptions();

    ScheduleExceptionDto createException(ScheduleExceptionRequest request);

    ScheduleExceptionDto updateException(UUID id, ScheduleExceptionRequest request);

    void deleteException(UUID id);

    List<AvailableSlotDto> computeAvailableSlots(LocalDate from, LocalDate to);
}
