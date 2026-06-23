-- ══════════════════════════════════════════════════════════════
--  File này chạy TỰ ĐỘNG lần đầu khi MySQL container khởi tạo.
--  Nếu database đã tồn tại (volume cũ), file này KHÔNG chạy lại.
-- ══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS e0bmanager_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE e0bmanager_db;

-- Spring Boot + Hibernate (ddl-auto=update) sẽ tự tạo các bảng.
-- Đặt các câu ALTER hoặc INSERT data mẫu ở đây nếu cần:

-- Ví dụ: thêm cột avatar và email nếu bảng đã tồn tại từ trước
-- (An toàn vì dùng IF NOT EXISTS / IGNORE)
-- ALTER TABLE order_account
--   ADD COLUMN IF NOT EXISTS email  VARCHAR(255) NULL,
--   ADD COLUMN IF NOT EXISTS avatar LONGTEXT     NULL;
