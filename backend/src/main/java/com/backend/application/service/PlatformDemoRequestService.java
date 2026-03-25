package com.backend.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.platform.PlatformDemoRequestAdminDto;
import com.backend.application.dto.platform.PlatformDemoRequestSubmitCommand;

public interface PlatformDemoRequestService {

    void submit(PlatformDemoRequestSubmitCommand command);

    Page<PlatformDemoRequestAdminDto> getPage(Pageable pageable, String search);
}
