-- Remove duplicate site_settings rows introduced by ImpEx scripts running
-- ON DUPLICATE KEY UPDATE against NULL-language rows. MySQL treats each NULL
-- as unique in a UNIQUE index, so duplicate inserts were not caught.
-- Keep the row with the lowest id in each (setting_key, language) group.

DELETE s1 FROM site_settings s1
INNER JOIN site_settings s2
  ON s1.setting_key = s2.setting_key
 AND s1.language IS NULL
 AND s2.language IS NULL
 AND s1.id > s2.id;

DELETE s1 FROM site_settings s1
INNER JOIN site_settings s2
  ON s1.setting_key = s2.setting_key
 AND s1.language IS NOT NULL
 AND s1.language = s2.language
 AND s1.id > s2.id;
