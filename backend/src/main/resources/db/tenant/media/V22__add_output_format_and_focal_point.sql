-- ============================================
-- Media Module Migration V22
-- Description: Add output_format to formats and focal point to media
-- ============================================

-- Add output_format to media_formats table
ALTER TABLE media_formats
ADD COLUMN output_format VARCHAR(20) DEFAULT 'ORIGINAL';

-- Add focal point columns to media table for smart cropping
ALTER TABLE media
ADD COLUMN focal_point_x DOUBLE DEFAULT 0.5,
ADD COLUMN focal_point_y DOUBLE DEFAULT 0.5;

-- Add index for faster focal point queries (optional, for future use)
CREATE INDEX idx_media_focal_point ON media (focal_point_x, focal_point_y);
