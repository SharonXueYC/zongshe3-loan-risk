package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.PortalSettingsDTO;
import com.example.zongshe1.dto.SystemSettingsDTO;
import com.example.zongshe1.service.SettingsService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SettingsServiceImpl implements SettingsService {

    // 模拟数据库存储的设置
    private final Map<String, Object> settings = new HashMap<>();
    private PortalSettingsDTO portalSettings;

    @Value("${app.system.min-loan-amount:1000}")
    private double minLoanAmount;

    @Value("${app.system.max-loan-amount:500000}")
    private double maxLoanAmount;

    @PostConstruct
    public void init() {
        // 初始化默认设置
        settings.put("systemName", "个人贷款管理系统");
        settings.put("systemVersion", "1.0.0");
        settings.put("systemDescription", "个人贷款管理系统是一款专为金融机构设计的贷款业务管理平台，提供贷款申请、审批、管理等功能。");
        settings.put("passwordPolicy", "medium");
        settings.put("loginAttempts", 5);
        settings.put("sessionTimeout", 30);
        settings.put("smtpServer", "smtp.example.com");
        settings.put("smtpPort", 587);
        settings.put("emailFrom", "noreply@example.com");
        portalSettings = defaultPortalSettings();
    }

    @Override
    public SystemSettingsDTO getSystemSettings() {
        SystemSettingsDTO dto = new SystemSettingsDTO();
        dto.setSystemName((String) settings.get("systemName"));
        dto.setSystemVersion((String) settings.get("systemVersion"));
        dto.setSystemDescription((String) settings.get("systemDescription"));
        dto.setPasswordPolicy((String) settings.get("passwordPolicy"));
        dto.setLoginAttempts((Integer) settings.get("loginAttempts"));
        dto.setSessionTimeout((Integer) settings.get("sessionTimeout"));
        dto.setSmtpServer((String) settings.get("smtpServer"));
        dto.setSmtpPort((Integer) settings.get("smtpPort"));
        dto.setEmailFrom((String) settings.get("emailFrom"));
        return dto;
    }

    @Override
    public SystemSettingsDTO updateSystemSettings(SystemSettingsDTO settingsDTO) {
        if (settingsDTO.getSystemName() != null) {
            settings.put("systemName", settingsDTO.getSystemName());
        }
        if (settingsDTO.getSystemVersion() != null) {
            settings.put("systemVersion", settingsDTO.getSystemVersion());
        }
        if (settingsDTO.getSystemDescription() != null) {
            settings.put("systemDescription", settingsDTO.getSystemDescription());
        }
        if (settingsDTO.getPasswordPolicy() != null) {
            settings.put("passwordPolicy", settingsDTO.getPasswordPolicy());
        }
        if (settingsDTO.getLoginAttempts() != null) {
            settings.put("loginAttempts", settingsDTO.getLoginAttempts());
        }
        if (settingsDTO.getSessionTimeout() != null) {
            settings.put("sessionTimeout", settingsDTO.getSessionTimeout());
        }
        if (settingsDTO.getSmtpServer() != null) {
            settings.put("smtpServer", settingsDTO.getSmtpServer());
        }
        if (settingsDTO.getSmtpPort() != null) {
            settings.put("smtpPort", settingsDTO.getSmtpPort());
        }
        if (settingsDTO.getEmailFrom() != null) {
            settings.put("emailFrom", settingsDTO.getEmailFrom());
        }

        log.info("系统设置已更新: {}", settingsDTO);
        return getSystemSettings();
    }

    @Override
    public SystemSettingsDTO resetSystemSettings() {
        // 清除所有设置并重新初始化
        settings.clear();
        init();

        log.info("系统设置已重置为默认值");
        return getSystemSettings();
    }

    @Override
    public PortalSettingsDTO getPortalSettings() {
        if (portalSettings == null) {
            portalSettings = defaultPortalSettings();
        }
        return portalSettings;
    }

    @Override
    public PortalSettingsDTO updatePortalSettings(PortalSettingsDTO settingsDto) {
        if (settingsDto == null) {
            return getPortalSettings();
        }
        portalSettings = settingsDto;
        if (settingsDto.getBasic() != null) {
            settings.put("systemName", settingsDto.getBasic().getSystemName());
            settings.put("systemVersion", settingsDto.getBasic().getVersion());
            settings.put("systemDescription", settingsDto.getBasic().getDescription());
        }
        if (settingsDto.getSecurity() != null && settingsDto.getSecurity().getMaxLoginAttempts() != null) {
            settings.put("loginAttempts", settingsDto.getSecurity().getMaxLoginAttempts());
        }
        log.info("管理端设置已更新");
        return portalSettings;
    }

    @Override
    public PortalSettingsDTO resetPortalSettings() {
        portalSettings = defaultPortalSettings();
        return portalSettings;
    }

    private PortalSettingsDTO defaultPortalSettings() {
        PortalSettingsDTO dto = new PortalSettingsDTO();

        PortalSettingsDTO.BasicSettings basic = new PortalSettingsDTO.BasicSettings();
        basic.setSystemName((String) settings.getOrDefault("systemName", "闪借贷款管理系统"));
        basic.setVersion((String) settings.getOrDefault("systemVersion", "1.0.0"));
        basic.setDescription((String) settings.getOrDefault("systemDescription", "专业的个人贷款管理平台"));
        basic.setPhone("400-888-8888");
        basic.setEmail("support@shanjie.com");
        basic.setAddress("北京市朝阳区金融街88号");
        dto.setBasic(basic);

        PortalSettingsDTO.LoanSettings loan = new PortalSettingsDTO.LoanSettings();
        loan.setMinRate(4.5);
        loan.setMaxRate(18.0);
        loan.setDefaultRate(10.0);
        loan.setMinAmount((int) minLoanAmount);
        loan.setMaxAmount((int) maxLoanAmount);
        loan.setMinTerm(1);
        loan.setMaxTerm(36);
        dto.setLoan(loan);

        PortalSettingsDTO.NotificationSettings notification = new PortalSettingsDTO.NotificationSettings();
        notification.setRemindDays(3);
        notification.setOverdueInterval(24);
        notification.setSms(true);
        notification.setEmail(true);
        notification.setWechat(false);
        dto.setNotification(notification);

        PortalSettingsDTO.SecuritySettings security = new PortalSettingsDTO.SecuritySettings();
        security.setMinPasswordLength(8);
        security.setPasswordExpireDays(90);
        security.setRequireUpper(true);
        security.setRequireLower(true);
        security.setRequireNumber(true);
        security.setRequireSpecial(false);
        security.setMaxLoginAttempts((Integer) settings.getOrDefault("loginAttempts", 5));
        security.setLockoutMinutes(15);
        security.setEnableCaptcha(false);
        security.setEnableTwoFactor(false);
        dto.setSecurity(security);

        return dto;
    }
}