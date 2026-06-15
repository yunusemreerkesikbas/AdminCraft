package com.backend.domain.commerce.repository;

import java.time.LocalDate;

public interface CommerceOrderNumberCounterRepository {

	int nextSequence(String prefix, LocalDate orderDate);
}
