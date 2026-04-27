package com.backend.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.contact.ContactRequestAdminDto;
import com.backend.application.dto.contact.ContactRequestSubmitCommand;

public interface ContactRequestService {

    void submit(ContactRequestSubmitCommand command);

    Page<ContactRequestAdminDto> getPage(Pageable pageable, String search);
}
