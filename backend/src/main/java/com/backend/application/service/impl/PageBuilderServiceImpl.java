package com.backend.application.service.impl;

import com.backend.application.service.PageBuilderService;
import com.backend.domain.entity.PageBlock;
import com.backend.domain.entity.PageSection;
import com.backend.domain.repository.PageBlockRepository;
import com.backend.domain.repository.PageSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PageBuilderServiceImpl implements PageBuilderService {

  private final PageSectionRepository sectionRepository;
  private final PageBlockRepository blockRepository;

  @Override
  public PageSection addSection(Long pageId, String type, Integer order, String data) {
    PageSection s = new PageSection();
    s.setPageId(pageId);
    s.setType(type);
    s.setDisplayOrder(order == null ? 0 : order);
    s.setData(data);
    return sectionRepository.save(s);
  }

  @Override
  public PageSection updateSection(Long sectionId, String type, Integer order, String data) {
    PageSection s = sectionRepository.findById(sectionId)
        .orElseThrow(() -> new IllegalArgumentException("Section not found"));
    if (type != null)
      s.setType(type);
    if (order != null)
      s.setDisplayOrder(order);
    if (data != null)
      s.setData(data);
    return sectionRepository.save(s);
  }

  @Override
  public void removeSection(Long sectionId) {
    sectionRepository.deleteById(sectionId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageSection> listSections(Long pageId) {
    return sectionRepository.findByPageIdOrderByDisplayOrderAsc(pageId);
  }

  @Override
  public PageBlock addBlock(Long sectionId, String type, Integer order, String data) {
    PageBlock b = new PageBlock();
    b.setSectionId(sectionId);
    b.setType(type);
    b.setDisplayOrder(order == null ? 0 : order);
    b.setData(data);
    return blockRepository.save(b);
  }

  @Override
  public PageBlock updateBlock(Long blockId, String type, Integer order, String data) {
    PageBlock b = blockRepository.findById(blockId)
        .orElseThrow(() -> new IllegalArgumentException("Block not found"));
    if (type != null)
      b.setType(type);
    if (order != null)
      b.setDisplayOrder(order);
    if (data != null)
      b.setData(data);
    return blockRepository.save(b);
  }

  @Override
  public void removeBlock(Long blockId) {
    blockRepository.deleteById(blockId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PageBlock> listBlocks(Long sectionId) {
    return blockRepository.findBySectionIdOrderByDisplayOrderAsc(sectionId);
  }
}
