ALTER TABLE attendance_records
    ADD COLUMN check_in_image_path VARCHAR(500) NULL,
    ADD COLUMN check_out_image_path VARCHAR(500) NULL;