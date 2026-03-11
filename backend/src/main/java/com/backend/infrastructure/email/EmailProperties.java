package com.backend.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.backend.domain.port.MailConfigPort;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties implements MailConfigPort {

    private boolean enabled = true;
    private String provider = "console";
    private String fromAddress = "noreply@admincraft.com";
    private String fromName = "AdminCraft";
    private boolean logContent = false;
    private boolean logSimplified = true;
}
