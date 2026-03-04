-- 1. Xóa bảng cũ nếu tồn tại để tránh xung đột cấu trúc
DROP TABLE IF EXISTS users CASCADE;

-- 2. Tạo bảng users khớp với Entity Java
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) -- Lưu giá trị String của Enum Role
);