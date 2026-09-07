package com.example.zongshe1.service;

import com.example.zongshe1.dto.PortalSettingsDTO;
import com.example.zongshe1.dto.SystemSettingsDTO;

public interface SettingsService {

    SystemSettingsDTO getSystemSettings();

    SystemSettingsDTO updateSystemSettings(SystemSettingsDTO settingsDTO);

    SystemSettingsDTO resetSystemSettings();

    PortalSettingsDTO getPortalSettings();

    PortalSettingsDTO updatePortalSettings(PortalSettingsDTO settings);

    PortalSettingsDTO resetPortalSettings();
}