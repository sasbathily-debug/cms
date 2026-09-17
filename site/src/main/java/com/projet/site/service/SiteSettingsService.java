package com.projet.site.service;

import com.projet.site.dto.SiteSettingsDto;
import com.projet.site.dto.UpdateSiteSettingsRequest;

public interface SiteSettingsService {

    SiteSettingsDto get();

    SiteSettingsDto update(UpdateSiteSettingsRequest request);
}
