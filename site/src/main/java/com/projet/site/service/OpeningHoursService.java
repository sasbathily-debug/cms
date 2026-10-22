package com.projet.site.service;

import com.projet.site.dto.OpeningHourDto;
import com.projet.site.dto.UpdateOpeningHoursRequest;

import java.util.List;

public interface OpeningHoursService {

    /** Les sept jours, du lundi au dimanche. */
    List<OpeningHourDto> findAll();

    /** Remplace les horaires des sept jours. Refuse un jour manquant ou en double, ou une plage où l'ouverture n'est pas avant la fermeture. */
    List<OpeningHourDto> update(UpdateOpeningHoursRequest request);
}
