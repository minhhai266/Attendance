-- Thêm cột check_status (bitmask int) cho AttendanceCheckStatus
ALTER TABLE attendance_records
    ADD COLUMN check_status INT NULL;

-- Backfill: record có status = LATE (value 2) cũ → chuyển thành PRESENT (value 1) + check_status chứa LATE (bit 1)
UPDATE attendance_records
SET status = 1,
    check_status = 1
WHERE status = 2;

-- Backfill: record có note chứa "Về sớm" → thêm EARLY_LEAVE (bit 2) vào check_status
UPDATE attendance_records
SET check_status = COALESCE(check_status, 0) | 2
WHERE note LIKE '%Về sớm%';

-- Mặc định check_status = 0 (đúng giờ) cho các record còn lại
UPDATE attendance_records
SET check_status = COALESCE(check_status, 0)
WHERE check_status IS NULL;