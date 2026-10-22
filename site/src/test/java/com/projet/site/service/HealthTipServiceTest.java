package com.projet.site.service;

import com.projet.site.domain.HealthTip;
import com.projet.site.dto.HealthTipDto;
import com.projet.site.dto.HealthTipRequest;
import com.projet.site.mapper.HealthTipMapper;
import com.projet.site.repository.HealthTipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HealthTipServiceTest {

    private HealthTipRepository repository;
    private HealthTipMapper mapper;
    private HealthTipServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(HealthTipRepository.class);
        mapper = mock(HealthTipMapper.class);
        service = new HealthTipServiceImpl(repository, mapper);
        when(repository.save(any(HealthTip.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private HealthTip saved() {
        ArgumentCaptor<HealthTip> captor = ArgumentCaptor.forClass(HealthTip.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void unArticleSansIndicationDePublicationEstPublie() {
        service.create(new HealthTipRequest("Titre", "Texte", null, 1, null, null, null, null));

        assertThat(saved().isPublished()).isTrue();
    }

    @Test
    void unBrouillonNEstPasPublie() {
        service.create(new HealthTipRequest("Titre", "Texte", null, 1, "Diabète", 5, "/img/a.jpg", false));

        HealthTip tip = saved();
        assertThat(tip.isPublished()).isFalse();
        assertThat(tip.getCategory()).isEqualTo("Diabète");
        assertThat(tip.getReadingMinutes()).isEqualTo(5);
        assertThat(tip.getPhotoUrl()).isEqualTo("/img/a.jpg");
    }

    @Test
    void lesChampsVidesDeviennentNull() {
        service.create(new HealthTipRequest("Titre", "Texte", null, 1, "  ", null, "   ", true));

        HealthTip tip = saved();
        assertThat(tip.getCategory()).isNull();
        assertThat(tip.getPhotoUrl()).isNull();
    }

    @Test
    void lePublicNeVoitQueLesArticlesPublies() {
        HealthTip published = mock(HealthTip.class);
        HealthTipDto dto = new HealthTipDto(null, "t", "b", null, 1, null, null, null, true);
        when(repository.findAllByPublishedTrueOrderByDisplayOrderAsc()).thenReturn(List.of(published));
        when(mapper.toDto(published)).thenReturn(dto);

        assertThat(service.findAll()).containsExactly(dto);
        verify(repository, never()).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    void lAdminVoitAussiLesBrouillons() {
        when(repository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of());

        assertThat(service.findAllForAdmin()).isEmpty();
        verify(repository).findAllByOrderByDisplayOrderAsc();
        verify(repository, never()).findAllByPublishedTrueOrderByDisplayOrderAsc();
    }
}
