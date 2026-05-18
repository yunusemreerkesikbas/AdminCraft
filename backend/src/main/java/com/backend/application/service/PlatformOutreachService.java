package com.backend.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.dto.email.EmailResult;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.enums.OutreachCampaignContactStatus;
import com.backend.domain.enums.OutreachCampaignStatus;
import com.backend.domain.enums.OutreachContactStatus;
import com.backend.domain.port.MailConfigPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachCampaign;
import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachCampaignContact;
import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachContact;
import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachTemplate;
import com.backend.infrastructure.persistence.platform.repository.PlatformOutreachCampaignContactRepository;
import com.backend.infrastructure.persistence.platform.repository.PlatformOutreachCampaignRepository;
import com.backend.infrastructure.persistence.platform.repository.PlatformOutreachContactRepository;
import com.backend.infrastructure.persistence.platform.repository.PlatformOutreachTemplateRepository;
import com.backend.presentation.dto.request.outreach.CreateOutreachCampaignRequest;
import com.backend.presentation.dto.request.outreach.CreateOutreachContactRequest;
import com.backend.presentation.dto.request.outreach.CreateOutreachTemplateRequest;
import com.backend.presentation.dto.request.outreach.UpdateOutreachContactRequest;
import com.backend.presentation.dto.request.outreach.UpdateOutreachTemplateRequest;
import com.backend.presentation.dto.response.outreach.OutreachCampaignOutboxEntryResponse;
import com.backend.presentation.dto.response.outreach.OutreachCampaignResponse;
import com.backend.presentation.dto.response.outreach.OutreachContactResponse;
import com.backend.presentation.dto.response.outreach.OutreachTemplateResponse;
import com.backend.shared.common.LogSanitizer;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformOutreachService {

    private final PlatformOutreachContactRepository contactRepository;
    private final PlatformOutreachTemplateRepository templateRepository;
    private final PlatformOutreachCampaignRepository campaignRepository;
    private final PlatformOutreachCampaignContactRepository campaignContactRepository;
    private final MailSenderPort mailSender;
    private final TemplateVariableRenderer templateVariableRenderer;
    private final MailConfigPort mailConfigPort;
    private final SecurityHelper securityHelper;
    @Qualifier("platformTransactionManager")
    private final PlatformTransactionManager platformTransactionManager;

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public Page<OutreachContactResponse> getContacts(int page, int size, String sort, String search) {
        Sort sortObj = parseSortOrDefault(sort, "createdAt,desc");
        var pageable = PageRequest.of(page, size, sortObj);
        Page<PlatformOutreachContact> resultPage = contactRepository.searchContacts(search, pageable);
        List<OutreachContactResponse> content = resultPage.getContent().stream()
            .map(OutreachContactResponse::from)
            .toList();
        return new PageImpl<>(content, pageable, resultPage.getTotalElements());
    }

    @Transactional("platformTransactionManager")
    public OutreachContactResponse createContact(CreateOutreachContactRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (contactRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("outreach.contact.email.exists");
        }
        PlatformOutreachContact contact = new PlatformOutreachContact();
        contact.setFullName(req.fullName().trim());
        contact.setEmail(normalizedEmail);
        contact.setCompanyName(req.companyName());
        contact.setCity(req.city());
        contact.setNotes(req.notes());
        contact.setStatus(OutreachContactStatus.ACTIVE);
        return OutreachContactResponse.from(contactRepository.save(contact));
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public OutreachContactResponse getContact(Long id) {
        return OutreachContactResponse.from(loadContact(id));
    }

    @Transactional("platformTransactionManager")
    public OutreachContactResponse updateContact(Long id, UpdateOutreachContactRequest req) {
        PlatformOutreachContact contact = loadContact(id);
        String normalizedEmail = req.email().trim().toLowerCase();
        if (!normalizedEmail.equalsIgnoreCase(contact.getEmail())
                && contactRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("outreach.contact.email.exists");
        }
        contact.setFullName(req.fullName().trim());
        contact.setEmail(normalizedEmail);
        contact.setCompanyName(req.companyName());
        contact.setCity(req.city());
        contact.setNotes(req.notes());
        if (req.status() != null && !req.status().isBlank()) {
            contact.setStatus(OutreachContactStatus.valueOf(req.status().toUpperCase()));
        }
        return OutreachContactResponse.from(contactRepository.save(contact));
    }

    @Transactional("platformTransactionManager")
    public void deleteContact(Long id) {
        PlatformOutreachContact contact = loadContact(id);
        contact.setStatus(OutreachContactStatus.UNSUBSCRIBED);
        contactRepository.save(contact);
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public List<OutreachTemplateResponse> getTemplates() {
        return templateRepository.findAll().stream()
            .map(OutreachTemplateResponse::from)
            .toList();
    }

    @Transactional("platformTransactionManager")
    public OutreachTemplateResponse createTemplate(CreateOutreachTemplateRequest req) {
        PlatformOutreachTemplate template = new PlatformOutreachTemplate();
        template.setName(req.name().trim());
        template.setSubject(req.subject().trim());
        template.setContent(req.content());
        template.setLanguage(req.language().toUpperCase());
        template.setIsActive(Boolean.TRUE);
        return OutreachTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public OutreachTemplateResponse getTemplate(Long id) {
        return OutreachTemplateResponse.from(loadTemplate(id));
    }

    @Transactional("platformTransactionManager")
    public OutreachTemplateResponse updateTemplate(Long id, UpdateOutreachTemplateRequest req) {
        PlatformOutreachTemplate template = loadTemplate(id);
        template.setName(req.name().trim());
        template.setSubject(req.subject().trim());
        template.setContent(req.content());
        template.setLanguage(req.language().toUpperCase());
        if (req.isActive() != null) {
            template.setIsActive(req.isActive());
        }
        return OutreachTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional("platformTransactionManager")
    public void deleteTemplate(Long id) {
        PlatformOutreachTemplate template = loadTemplate(id);
        if (campaignRepository.existsByTemplateId(id)) {
            template.setIsActive(Boolean.FALSE);
            templateRepository.save(template);
        } else {
            templateRepository.delete(template);
        }
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public Page<OutreachCampaignResponse> getCampaigns(int page, int size, String sort) {
        Sort sortObj = parseSortOrDefault(sort, "createdAt,desc");
        var pageable = PageRequest.of(page, size, sortObj);
        Page<PlatformOutreachCampaign> resultPage = campaignRepository.findAll(pageable);
        List<OutreachCampaignResponse> content = resultPage.getContent().stream()
            .map(OutreachCampaignResponse::from)
            .toList();
        return new PageImpl<>(content, pageable, resultPage.getTotalElements());
    }

    @Transactional("platformTransactionManager")
    public OutreachCampaignResponse createCampaign(CreateOutreachCampaignRequest req) {
        PlatformOutreachTemplate template = loadTemplate(req.templateId());
        List<PlatformOutreachContact> contacts = contactRepository.findByIdIn(req.contactIds());
        if (contacts.isEmpty()) {
            throw new IllegalArgumentException("outreach.campaign.contacts.empty");
        }

        String subject = (req.subjectOverride() != null && !req.subjectOverride().isBlank())
            ? req.subjectOverride().trim()
            : template.getSubject();

        PlatformOutreachCampaign campaign = new PlatformOutreachCampaign();
        campaign.setName(req.name().trim());
        campaign.setTemplate(template);
        campaign.setSubject(subject);
        campaign.setStatus(OutreachCampaignStatus.DRAFT);
        campaign.setTotalCount(contacts.size());
        campaign.setSentCount(0);
        campaign.setFailedCount(0);
        campaign.setCreatedByEmail(resolveCurrentUserEmail());
        PlatformOutreachCampaign savedCampaign = campaignRepository.save(campaign);

        List<PlatformOutreachCampaignContact> entries = contacts.stream().map(contact -> {
            PlatformOutreachCampaignContact cc = new PlatformOutreachCampaignContact();
            cc.setCampaign(savedCampaign);
            cc.setContact(contact);
            cc.setStatus(OutreachCampaignContactStatus.PENDING);
            return cc;
        }).toList();
        campaignContactRepository.saveAll(entries);

        return OutreachCampaignResponse.from(savedCampaign);
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public OutreachCampaignResponse getCampaign(Long id) {
        return OutreachCampaignResponse.from(loadCampaign(id));
    }

    public OutreachCampaignResponse sendCampaign(Long id) {
        TransactionTemplate tx = new TransactionTemplate(platformTransactionManager);

        record SendSetup(Long campaignId, List<PlatformOutreachCampaignContact> contacts) {}

        SendSetup setup = tx.execute(status -> {
            PlatformOutreachCampaign campaign = campaignRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("outreach.campaign.not.found"));
            if (campaign.getStatus() != OutreachCampaignStatus.DRAFT) {
                throw new IllegalStateException("outreach.campaign.already.sent");
            }
            campaign.setStatus(OutreachCampaignStatus.SENDING);
            campaignRepository.save(campaign);

            List<PlatformOutreachCampaignContact> contacts = campaignContactRepository.findByCampaignId(id);
            return new SendSetup(id, contacts);
        });

        int sent = 0;
        int failed = 0;

        try {
            for (PlatformOutreachCampaignContact detachedCc : setup.contacts()) {
                boolean success = Boolean.TRUE.equals(tx.execute(status -> {
                    PlatformOutreachCampaignContact cc = campaignContactRepository.findById(detachedCc.getId())
                        .orElseThrow(() -> new IllegalStateException("outreach.campaign.contact.not.found"));
                    PlatformOutreachContact contact = detachedCc.getContact();
                    PlatformOutreachCampaign campaign = cc.getCampaign();

                    String fromName = mailConfigPort.getFromName();
                    Map<String, String> vars = Map.of(
                        "contactName", nullToEmpty(contact.getFullName()),
                        "companyName", nullToEmpty(contact.getCompanyName()),
                        "city", nullToEmpty(contact.getCity()),
                        "email", nullToEmpty(contact.getEmail()),
                        "fromName", nullToEmpty(fromName)
                    );

                    String renderedSubject = templateVariableRenderer.render(campaign.getSubject(), vars);
                    String renderedContent = campaign.getTemplate() != null
                        ? templateVariableRenderer.render(campaign.getTemplate().getContent(), vars)
                        : "";

                    cc.setRenderedSubject(renderedSubject);
                    cc.setRenderedContent(renderedContent);

                    EmailResult result = mailSender.send(contact.getEmail(), renderedSubject, renderedContent);
                    if (result.isSuccess()) {
                        cc.setStatus(OutreachCampaignContactStatus.SENT);
                        cc.setProviderMessageId(result.getMessageId());
                    } else {
                        cc.setStatus(OutreachCampaignContactStatus.FAILED);
                        cc.setErrorMessage(LogSanitizer.sanitizeForLog(result.getErrorMessage()));
                    }
                    campaignContactRepository.save(cc);
                    return result.isSuccess();
                }));
                if (success) {
                    sent++;
                } else {
                    failed++;
                }
            }
        } catch (Exception e) {
            log.error("Campaign {} failed catastrophically during send, marking as FAILED", setup.campaignId(), e);
            tx.execute(status -> {
                PlatformOutreachCampaign campaign = loadCampaign(setup.campaignId());
                campaign.setStatus(OutreachCampaignStatus.FAILED);
                campaignRepository.save(campaign);
                return null;
            });
            throw e;
        }

        int finalSent = sent;
        int finalFailed = failed;
        return tx.execute(status -> {
            PlatformOutreachCampaign campaign = loadCampaign(setup.campaignId());
            campaign.setSentCount(finalSent);
            campaign.setFailedCount(finalFailed);
            campaign.setStatus(finalFailed > 0
                ? OutreachCampaignStatus.COMPLETED_WITH_ERRORS
                : OutreachCampaignStatus.COMPLETED);
            return OutreachCampaignResponse.from(campaignRepository.save(campaign));
        });
    }

    @Transactional(value = "platformTransactionManager", readOnly = true)
    public Page<OutreachCampaignOutboxEntryResponse> getCampaignOutbox(Long id, int page, int size) {
        loadCampaign(id);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<PlatformOutreachCampaignContact> resultPage = campaignContactRepository.findByCampaignId(id, pageable);
        List<OutreachCampaignOutboxEntryResponse> content = resultPage.getContent().stream()
            .map(OutreachCampaignOutboxEntryResponse::from)
            .toList();
        return new PageImpl<>(content, pageable, resultPage.getTotalElements());
    }

    private PlatformOutreachContact loadContact(Long id) {
        return contactRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("outreach.contact.not.found"));
    }

    private PlatformOutreachTemplate loadTemplate(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("outreach.template.not.found"));
    }

    private PlatformOutreachCampaign loadCampaign(Long id) {
        return campaignRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("outreach.campaign.not.found"));
    }

    private String resolveCurrentUserEmail() {
        if (!securityHelper.isAuthenticated()) {
            return "system";
        }
        return securityHelper.getCurrentUserEmail();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private Sort parseSortOrDefault(String sort, String defaultSort) {
        String effective = (sort == null || sort.isBlank()) ? defaultSort : sort;
        String[] parts = effective.split(",");
        if (parts.length != 2) {
            parts = defaultSort.split(",");
        }
        String field = parts[0].trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(parts[1].trim())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
