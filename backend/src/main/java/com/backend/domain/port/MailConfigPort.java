package com.backend.domain.port;

public interface MailConfigPort {

    String getProvider();

    String getFromAddress();

    String getFromName();

    boolean isEnabled();

    boolean isLogSimplified();
}
