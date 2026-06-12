package com.backend.domain.commerce.repository;

import java.util.List;

import com.backend.domain.commerce.CommerceCustomerConsent;

public interface CommerceCustomerConsentRepository {

	List<CommerceCustomerConsent> saveAll(List<CommerceCustomerConsent> consents);
}
