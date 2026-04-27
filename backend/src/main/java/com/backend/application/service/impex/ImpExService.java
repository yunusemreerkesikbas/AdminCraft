package com.backend.application.service.impex;

import com.backend.application.dto.impex.ImpExResult;

import java.util.Locale;

public interface ImpExService {
    ImpExResult execute(String sqlContent, Locale locale, String clientIp);
}
