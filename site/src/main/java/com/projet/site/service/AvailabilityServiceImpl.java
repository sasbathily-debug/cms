package com.projet.site.service;

import com.projet.site.domain.Appointment;
import com.projet.site.domain.AppointmentStatus;
import com.projet.site.domain.AvailabilityRule;
import com.projet.site.domain.ScheduleException;
import com.projet.site.dto.AvailabilityRuleDto;
import com.projet.site.dto.AvailabilityRuleRequest;
import com.projet.site.dto.AvailableSlotDto;
import com.projet.site.dto.ScheduleExceptionDto;
import com.projet.site.dto.ScheduleExceptionRequest;
import com.projet.site.exception.DuplicateAvailabilityRuleException;
import com.projet.site.exception.DuplicateScheduleExceptionException;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.AvailabilityRuleMapper;
import com.projet.site.mapper.ScheduleExceptionMapper;
import com.projet.site.repository.AppointmentRepository;
import com.projet.site.repository.AvailabilityRuleRepository;
import com.projet.site.repository.ScheduleExceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final int MAX_RANGE_DAYS = 60;
    private static final int DEFAULT_SLOT_DURATION_MINUTES = 30;

    private final AvailabilityRuleRepository ruleRepository;
    private final ScheduleExceptionRepository exceptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityRuleMapper ruleMapper;
    private final ScheduleExceptionMapper exceptionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityRuleDto> findAllRules() {
        return ruleRepository.findAllByOrderByDayOfWeekAscStartTimeAsc().stream().map(ruleMapper::toDto).toList();
    }

    @Override
    public AvailabilityRuleDto createRule(AvailabilityRuleRequest request) {
        if (ruleRepository.existsByDayOfWeekAndStartTimeAndEndTime(
                request.dayOfWeek(), request.startTime(), request.endTime())) {
            throw new DuplicateAvailabilityRuleException(request.dayOfWeek(), request.startTime(), request.endTime());
        }
        AvailabilityRule rule = new AvailabilityRule(
                request.dayOfWeek(), request.startTime(), request.endTime(), request.slotDurationMinutes());
        return ruleMapper.toDto(ruleRepository.save(rule));
    }

    @Override
    public AvailabilityRuleDto updateRule(UUID id, AvailabilityRuleRequest request) {
        AvailabilityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Règle introuvable : " + id));
        if (ruleRepository.existsByDayOfWeekAndStartTimeAndEndTimeAndIdNot(
                request.dayOfWeek(), request.startTime(), request.endTime(), id)) {
            throw new DuplicateAvailabilityRuleException(request.dayOfWeek(), request.startTime(), request.endTime());
        }
        rule.setDayOfWeek(request.dayOfWeek());
        rule.setStartTime(request.startTime());
        rule.setEndTime(request.endTime());
        rule.setSlotDurationMinutes(request.slotDurationMinutes());
        return ruleMapper.toDto(ruleRepository.save(rule));
    }

    @Override
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Règle introuvable : " + id);
        }
        ruleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleExceptionDto> findAllExceptions() {
        return exceptionRepository.findAllByOrderByDateAsc().stream().map(exceptionMapper::toDto).toList();
    }

    @Override
    public ScheduleExceptionDto createException(ScheduleExceptionRequest request) {
        exceptionRepository.findByDate(request.date()).ifPresent(e -> {
            throw new DuplicateScheduleExceptionException(request.date());
        });
        ScheduleException exception = new ScheduleException(
                request.date(), request.closed(), request.customStartTime(), request.customEndTime(),
                request.slotDurationMinutes(), request.note());
        return exceptionMapper.toDto(exceptionRepository.save(exception));
    }

    @Override
    public ScheduleExceptionDto updateException(UUID id, ScheduleExceptionRequest request) {
        ScheduleException exception = exceptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exception introuvable : " + id));
        exception.setDate(request.date());
        exception.setClosed(request.closed());
        exception.setCustomStartTime(request.customStartTime());
        exception.setCustomEndTime(request.customEndTime());
        exception.setSlotDurationMinutes(request.slotDurationMinutes());
        exception.setNote(request.note());
        return exceptionMapper.toDto(exceptionRepository.save(exception));
    }

    @Override
    public void deleteException(UUID id) {
        if (!exceptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exception introuvable : " + id);
        }
        exceptionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlotDto> computeAvailableSlots(LocalDate from, LocalDate to) {
        LocalDate rangeStart = from;
        LocalDate rangeEnd = to;
        if (rangeEnd.isBefore(rangeStart)) {
            rangeEnd = rangeStart;
        }
        if (rangeEnd.isAfter(rangeStart.plusDays(MAX_RANGE_DAYS))) {
            rangeEnd = rangeStart.plusDays(MAX_RANGE_DAYS);
        }

        Map<LocalDate, ScheduleException> exceptionsByDate = exceptionRepository
                .findByDateBetween(rangeStart, rangeEnd).stream()
                .collect(Collectors.toMap(ScheduleException::getDate, e -> e));

        Map<DayOfWeek, List<AvailabilityRule>> rulesByDay = ruleRepository.findAll().stream()
                .collect(Collectors.groupingBy(AvailabilityRule::getDayOfWeek));

        Set<String> booked = appointmentRepository
                .findByPreferredDateBetweenAndStatusIn(
                        rangeStart, rangeEnd, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                .stream()
                .filter(a -> a.getPreferredDate() != null && a.getPreferredTime() != null)
                .map(a -> bookedKey(a.getPreferredDate(), a.getPreferredTime()))
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<AvailableSlotDto> slots = new ArrayList<>();
        Set<String> addedSlots = new HashSet<>();

        for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {
            ScheduleException exception = exceptionsByDate.get(date);
            if (exception != null && exception.isClosed()) {
                continue;
            }

            List<TimeRange> ranges;
            if (exception != null && exception.getCustomStartTime() != null && exception.getCustomEndTime() != null) {
                int duration = exception.getSlotDurationMinutes() != null
                        ? exception.getSlotDurationMinutes()
                        : DEFAULT_SLOT_DURATION_MINUTES;
                ranges = List.of(new TimeRange(exception.getCustomStartTime(), exception.getCustomEndTime(), duration));
            } else {
                ranges = rulesByDay.getOrDefault(date.getDayOfWeek(), List.of()).stream()
                        .map(r -> new TimeRange(r.getStartTime(), r.getEndTime(), r.getSlotDurationMinutes()))
                        .toList();
            }

            for (TimeRange range : ranges) {
                LocalTime t = range.start();
                while (!t.plusMinutes(range.durationMinutes()).isAfter(range.end())) {
                    LocalDate slotDate = date;
                    LocalTime slotTime = t;
                    boolean isPast = slotDate.isEqual(now.toLocalDate()) && slotTime.isBefore(now.toLocalTime());
                    boolean isBooked = booked.contains(bookedKey(slotDate, slotTime));
                    if (!isPast && !isBooked && addedSlots.add(bookedKey(slotDate, slotTime))) {
                        slots.add(new AvailableSlotDto(slotDate, slotTime));
                    }
                    t = t.plusMinutes(range.durationMinutes());
                }
            }
        }

        return slots;
    }

    private String bookedKey(LocalDate date, LocalTime time) {
        return date + "T" + time;
    }

    private record TimeRange(LocalTime start, LocalTime end, int durationMinutes) {
    }
}
