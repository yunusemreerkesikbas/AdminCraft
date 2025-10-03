package com.backend.application.service.impl;

import com.backend.application.service.ProvisioningService;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.JobStatus;
import com.backend.domain.enums.JobType;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.ProvisioningJobRepository;
import com.backend.presentation.dto.response.ProvisioningJobResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisioningServiceImpl implements ProvisioningService {

    private final ProvisioningJobRepository provisioningJobRepository;
    private final PageRepository pageRepository;
    private final PageI18nRepository pageI18nRepository;

    @Override
    @Transactional
    public ProvisioningJobResponse createLanguageProvisioningJob(Long tenantId, Set<Language> languages) {
        ProvisioningJob job = new ProvisioningJob();
        job.setUuid(UUID.randomUUID().toString());
        job.setTenantId(tenantId);
        job.setJobType(JobType.LANGUAGE_PROVISIONING);
        job.setStatus(JobStatus.PENDING);

        String languagesCsv = languages.stream()
                .map(Language::name)
                .collect(Collectors.joining(","));
        job.setLanguages(languagesCsv);

        job.setTotalItems(0);
        job.setProcessedItems(0);
        job.setFailedItems(0);

        job = provisioningJobRepository.save(job);

        executeLanguageProvisioning(job);

        return mapToResponse(job);
    }

    @Async
    @Override
    public CompletableFuture<Void> executeLanguageProvisioning(ProvisioningJob job) {
        try {
            updateJobStatus(job.getId(), JobStatus.RUNNING);

            Set<Language> languages = Arrays.stream(job.getLanguages().split(","))
                    .map(String::trim)
                    .map(Language::valueOf)
                    .collect(Collectors.toSet());

            List<Page> pages = pageRepository.findByTenantId(job.getTenantId());

            updateTotalItems(job.getId(), pages.size() * languages.size());

            int processed = 0;
            int failed = 0;

            for (Page page : pages) {
                for (Language language : languages) {
                    try {
                        createEmptyPageI18n(page, language);
                        processed++;
                    } catch (Exception e) {
                        log.error("Failed to create PageI18n for page {} and language {}",
                                page.getId(), language, e);
                        failed++;
                    }
                    updateProgress(job.getId(), processed, failed);
                }
            }

            completeJob(job.getId(), processed, failed);

        } catch (Exception e) {
            log.error("Provisioning job {} failed", job.getUuid(), e);
            failJob(job.getId(), e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ProvisioningJobResponse getJobStatus(String jobUuid) {
        ProvisioningJob job = provisioningJobRepository.findByUuid(jobUuid)
                .orElseThrow(() -> new RuntimeException("Provisioning job not found: " + jobUuid));

        return mapToResponse(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void createEmptyPageI18n(Page page, Language language) {
        boolean exists = pageI18nRepository.existsByPageIdAndLanguage(page.getId(), language);

        if (!exists) {
            PageI18n pageI18n = new PageI18n();
            pageI18n.setUuid(UUID.randomUUID().toString());
            pageI18n.setUid("cmsitem_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pageI18n.setPageId(page.getId());
            pageI18n.setTenantId(page.getTenantId());
            pageI18n.setLanguage(language);
            pageI18n.setStatus(PageStatus.DRAFT);

            pageI18nRepository.save(pageI18n);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateJobStatus(Long jobId, JobStatus status) {
        ProvisioningJob job = provisioningJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        if (status == JobStatus.RUNNING) {
            job.setStartedAt(LocalDateTime.now());
        }
        provisioningJobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateTotalItems(Long jobId, int totalItems) {
        ProvisioningJob job = provisioningJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setTotalItems(totalItems);
        provisioningJobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateProgress(Long jobId, int processed, int failed) {
        ProvisioningJob job = provisioningJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setProcessedItems(processed);
        job.setFailedItems(failed);
        provisioningJobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void completeJob(Long jobId, int processed, int failed) {
        ProvisioningJob job = provisioningJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setProcessedItems(processed);
        job.setFailedItems(failed);
        job.setStatus(failed > 0 && processed == 0 ? JobStatus.FAILED : JobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        provisioningJobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void failJob(Long jobId, String errorMessage) {
        ProvisioningJob job = provisioningJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.FAILED);
        job.setErrorMessage(errorMessage);
        job.setCompletedAt(LocalDateTime.now());
        provisioningJobRepository.save(job);
    }

    private ProvisioningJobResponse mapToResponse(ProvisioningJob job) {
        return new ProvisioningJobResponse(
                job.getUuid(),
                job.getTenantId(),
                job.getJobType(),
                job.getStatus(),
                job.getLanguages(),
                job.getTotalItems(),
                job.getProcessedItems(),
                job.getFailedItems(),
                (int) Math.round(job.getProgressPercentage()),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt());
    }
}
