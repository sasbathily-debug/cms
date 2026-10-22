package com.projet.site.service;

import com.projet.site.domain.OpeningHour;
import com.projet.site.dto.OpeningHourDto;
import com.projet.site.dto.UpdateOpeningHoursRequest;
import com.projet.site.exception.InvalidOpeningHoursException;
import com.projet.site.repository.OpeningHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OpeningHoursServiceImpl implements OpeningHoursService {

    private final OpeningHourRepository openingHourRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OpeningHourDto> findAll() {
        return openingHourRepository.findAllByOrderByDayOfWeekAsc().stream()
                .map(OpeningHoursServiceImpl::toDto)
                .toList();
    }

    @Override
    public List<OpeningHourDto> update(UpdateOpeningHoursRequest request) {
        validate(request.days());
        List<OpeningHour> hours = request.days().stream()
                .sorted(Comparator.comparingInt(OpeningHourDto::dayOfWeek))
                .map(day -> day.closed()
                        ? new OpeningHour(day.dayOfWeek(), null, null, true)
                        : new OpeningHour(day.dayOfWeek(), day.opens(), day.closes(), false))
                .toList();
        return openingHourRepository.saveAll(hours).stream()
                .sorted(Comparator.comparingInt(OpeningHour::getDayOfWeek))
                .map(OpeningHoursServiceImpl::toDto)
                .toList();
    }

    static void validate(List<OpeningHourDto> days) {
        Set<Integer> seen = days.stream().map(OpeningHourDto::dayOfWeek).collect(Collectors.toSet());
        if (seen.size() != 7) {
            throw new InvalidOpeningHoursException("Il faut exactement un horaire pour chacun des sept jours.");
        }
        for (OpeningHourDto day : days) {
            if (day.closed()) {
                continue;
            }
            if (day.opens() == null || day.closes() == null) {
                throw new InvalidOpeningHoursException(
                        "Indiquez l'heure d'ouverture et de fermeture, ou cochez « fermé » (jour " + day.dayOfWeek() + ").");
            }
            if (!day.opens().isBefore(day.closes())) {
                throw new InvalidOpeningHoursException(
                        "L'ouverture doit être avant la fermeture (jour " + day.dayOfWeek() + ").");
            }
        }
    }

    private static OpeningHourDto toDto(OpeningHour hour) {
        return new OpeningHourDto(hour.getDayOfWeek(), hour.getOpens(), hour.getCloses(), hour.isClosed());
    }
}
