package com.backend.application.service.impl;

import com.backend.domain.entity.MediaFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class StorageService {

  @Value("${admincraft.media.upload-path:uploads}")
  private String uploadPath;

  // Sprint 7: Tenant namespaced storage path
  // Format: /tenants/{tenantId}/media/{yyyy}/{MM}/{uuid}.{ext}
  public Path saveToTenantFolder(byte[] content, Long tenantId, String fileName)
      throws IOException {
    java.time.LocalDate now = java.time.LocalDate.now();
    String year = String.valueOf(now.getYear());
    String month = String.format("%02d", now.getMonthValue());
    
    // Sprint 7 path structure
    Path tenantDir = Paths.get(uploadPath, "tenants", tenantId.toString(), "media", year, month);
    Files.createDirectories(tenantDir);
    Path filePath = tenantDir.resolve(fileName);
    Files.write(filePath, content);
    return filePath;
  }

  public void generateThumbnail(MediaFile mediaFile, int maxWidth, int maxHeight)
      throws IOException {
    if (!mediaFile.isImage()) {
      return;
    }
    Path path = Paths.get(mediaFile.getFilePath());
    byte[] bytes = Files.readAllBytes(path);
    BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
    if (src == null) {
      log.warn("Unsupported image for thumbnail: {}", mediaFile.getFileName());
      return;
    }
    int[] size = fit(src.getWidth(), src.getHeight(), maxWidth, maxHeight);
    BufferedImage dst = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);
    Graphics2D g = dst.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(src, 0, 0, size[0], size[1], null);
    g.dispose();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(dst, mediaFile.getFileExtension(), out);
    // Sprint 7: Thumbnail path structure  
    java.time.LocalDate now = java.time.LocalDate.now();
    String year = String.valueOf(now.getYear());
    String month = String.format("%02d", now.getMonthValue());
    
    Path thumbPath = Paths.get(uploadPath, "tenants", mediaFile.getTenantId().toString(), 
        "media", year, month, "thumbnails", "thumb_" + mediaFile.getFileName());
    Files.write(thumbPath, out.toByteArray());
    mediaFile.setHasThumbnails(true);
    mediaFile.setThumbnailPath(thumbPath.toString());
    mediaFile.setWidth(src.getWidth());
    mediaFile.setHeight(src.getHeight());
  }

  private int[] fit(int w, int h, int maxW, int maxH) {
    double ratio = Math.min(maxW / (double) w, maxH / (double) h);
    int nw = Math.max(1, (int) Math.round(w * ratio));
    int nh = Math.max(1, (int) Math.round(h * ratio));
    return new int[] { nw, nh };
  }

  public void generateVariants(MediaFile mediaFile) throws IOException {
    if (!mediaFile.isImage()) {
      return;
    }
    Path path = Paths.get(mediaFile.getFilePath());
    byte[] bytes = Files.readAllBytes(path);
    BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
    if (src == null) {
      log.warn("Unsupported image for variants: {}", mediaFile.getFileName());
      return;
    }

    Map<String, Object> variants = new HashMap<>();

    // Desktop variant (width ~1200)
    int[] desktopSize = fit(src.getWidth(), src.getHeight(), 1200, 1200);
    BufferedImage desktopImg = new BufferedImage(
        desktopSize[0], desktopSize[1], BufferedImage.TYPE_INT_RGB);
    Graphics2D gd = desktopImg.createGraphics();
    gd.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    gd.drawImage(src, 0, 0, desktopSize[0], desktopSize[1], null);
    gd.dispose();
    ByteArrayOutputStream desktopOut = new ByteArrayOutputStream();
    ImageIO.write(desktopImg, mediaFile.getFileExtension(), desktopOut);
    Path desktopPath = Paths.get(uploadPath, "tenant_" + mediaFile.getTenantId(),
        "desktop_" + mediaFile.getFileName());
    Files.write(desktopPath, desktopOut.toByteArray());
    Map<String, Object> desktopMeta = new HashMap<>();
    desktopMeta.put("path", desktopPath.toString());
    desktopMeta.put("width", desktopSize[0]);
    desktopMeta.put("height", desktopSize[1]);
    variants.put("desktop", desktopMeta);

    // Mobile variant (width ~768)
    int[] mobileSize = fit(src.getWidth(), src.getHeight(), 768, 768);
    BufferedImage mobileImg = new BufferedImage(
        mobileSize[0], mobileSize[1], BufferedImage.TYPE_INT_RGB);
    Graphics2D gm = mobileImg.createGraphics();
    gm.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    gm.drawImage(src, 0, 0, mobileSize[0], mobileSize[1], null);
    gm.dispose();
    ByteArrayOutputStream mobileOut = new ByteArrayOutputStream();
    ImageIO.write(mobileImg, mediaFile.getFileExtension(), mobileOut);
    Path mobilePath = Paths.get(uploadPath, "tenant_" + mediaFile.getTenantId(),
        "mobile_" + mediaFile.getFileName());
    Files.write(mobilePath, mobileOut.toByteArray());
    Map<String, Object> mobileMeta = new HashMap<>();
    mobileMeta.put("path", mobilePath.toString());
    mobileMeta.put("width", mobileSize[0]);
    mobileMeta.put("height", mobileSize[1]);
    variants.put("mobile", mobileMeta);

    ObjectMapper mapper = new ObjectMapper();
    mediaFile.setVariants(mapper.writeValueAsString(variants));
  }
}
