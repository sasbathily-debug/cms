package com.projet.site.service;

import com.projet.site.domain.OpeningHour;
import com.projet.site.dto.OpeningHourDto;
import com.projet.site.dto.UpdateOpeningHoursRequest;
import com.projet.site.exception.InvalidOpeningHoursException;
import com.projet.site.repository.OpeningHourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpeningHoursServiceTest {

    private OpeningHourRepository repository;
    private OpeningHoursServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(OpeningHourRepository.class);
        service = new OpeningHoursServiceImpl(repository);
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static OpeningHourDto open(int day, String from, String to) {
        return new OpeningHourDto(day, LocalTime.parse(from), LocalTime.parse(to), false);
    }

    private static OpeningHourDto closed(int day) {
        return new OpeningHourDto(day, null, null, true);
    }

    private static List<OpeningHourDto> validWeek() {
        List<OpeningHourDto> week = new ArrayList<>();
        for (int day = 1; day <= 5; day++) {
            week.add(open(day, "09:00", "17:30"));
        }
        week.add(open(6, "09:00", "13:00"));
        week.add(closed(7));
        return week;
    }

    @Test
    void enregistreUneSemaineValideTrieeParJour() {
        List<OpeningHourDto> week = validWeek();
        Collections.reverse(week);

        List<OpeningHourDto> saved = service.update(new UpdateOpeningHoursRequest(week));

        assertThat(saved).extracting(OpeningHourDto::dayOfWeek).containsExactly(1, 2, 3, 4, 5, 6, 7);
        assertThat(saved.get(5).closes()).isEqualTo(LocalTime.of(13, 0));
        assertThat(saved.get(6).closed()).isTrue();
    }

    @Test
    void ignoreLesHeuresDunJourFerme() {
        List<OpeningHourDto> week = validWeek();
        week.set(6, new OpeningHourDto(7, LocalTime.of(8, 0), LocalTime.of(9, 0), true));

        service.update(new UpdateOpeningHoursRequest(week));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OpeningHour>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        OpeningHour sunday = captor.getValue().get(6);
        assertThat(sunday.isClosed()).isTrue();
        assertThat(sunday.getOpens()).isNull();
        assertThat(sunday.getCloses()).isNull();
    }

    @Test
    void refuseUnJourEnDouble() {
        List<OpeningHourDto> week = validWeek();
        week.set(6, open(1, "09:00", "12:00"));

        assertThatThrownBy(() -> service.update(new UpdateOpeningHoursRequest(week)))
                .isInstanceOf(InvalidOpeningHoursException.class)
                .hasMessageContaining("sept jours");
        verify(repository, never()).saveAll(any());
    }

    @Test
    void refuseUneOuvertureApresLaFermeture() {
        List<OpeningHourDto> week = validWeek();
        week.set(2, open(3, "18:00", "09:00"));

        assertThatThrownBy(() -> service.update(new UpdateOpeningHoursRequest(week)))
                .isInstanceOf(InvalidOpeningHoursException.class)
                .hasMessageContaining("avant la fermeture");
    }

    @Test
    void refuseUneOuvertureEgaleALaFermeture() {
        List<OpeningHourDto> week = validWeek();
        week.set(0, open(1, "09:00", "09:00"));

        assertThatThrownBy(() -> service.update(new UpdateOpeningHoursRequest(week)))
                .isInstanceOf(InvalidOpeningHoursException.class);
    }

    @Test
    void refuseUnJourOuvertSansHeures() {
        List<OpeningHourDto> week = validWeek();
        week.set(1, new OpeningHourDto(2, null, null, false));

        assertThatThrownBy(() -> service.update(new UpdateOpeningHoursRequest(week)))
                .isInstanceOf(InvalidOpeningHoursException.class)
                .hasMessageContaining("fermé");
    }
}
