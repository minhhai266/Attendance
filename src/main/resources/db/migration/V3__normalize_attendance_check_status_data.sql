-- Normalize legacy attendance display data.
-- The "Trạng thái" column is rendered from check_status:
--   NULL/0 = đúng giờ, 1 = đi muộn, 2 = về sớm, 3 = đi muộn + về sớm.

CREATE TABLE IF NOT EXISTS attendance_records_check_status_backup AS
SELECT id, status, check_status, note
FROM attendance_records;

UPDATE attendance_records
SET check_status = NULL
WHERE note REGEXP 'Đúng giờ|Đủ công|Trong ngưỡng|Ca làm bình thường|Có face id|Manager check-in';

UPDATE attendance_records
SET check_status = COALESCE(check_status, 0) | 1
WHERE note REGEXP 'Đi muộn|[Mm]uộn|Check-in muộn|Quên check-in|Tắc đường';

UPDATE attendance_records
SET check_status = COALESCE(check_status, 0) | 2
WHERE note LIKE '%Về sớm%';

UPDATE attendance_records
SET check_status = 1
WHERE note LIKE '%Tắc đường%'
  AND note NOT LIKE '%Về sớm%';
