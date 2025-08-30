package com.backend.application.service;

import com.backend.domain.entity.PageBlock;
import com.backend.domain.entity.PageSection;

import java.util.List;

public interface PageBuilderService {
  PageSection addSection(Long pageId, String type, Integer order, String data);

  PageSection updateSection(Long sectionId, String type, Integer order, String data);

  void removeSection(Long sectionId);

  List<PageSection> listSections(Long pageId);

  PageBlock addBlock(Long sectionId, String type, Integer order, String data);

  PageBlock updateBlock(Long blockId, String type, Integer order, String data);

  void removeBlock(Long blockId);

  List<PageBlock> listBlocks(Long sectionId);
}
