-- 1. Tạo bảng users (Chỉ lưu thông tin cần thiết cho Profile)
-- Lưu ý: Không có bảng roles và không có trường password ở đây!
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY, -- ID này sẽ được nhận từ Auth Service qua RabbitMQ
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    avatar_url TEXT,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bạn có thể giữ lại bảng outbox ở đây NẾU sau này
-- User Service muốn bắn tin ngược lại (ví dụ: khi User cập nhật Profile)
CREATE TABLE IF NOT EXISTS outbox (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- Bổ sung vào migration của User Service
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, role_id)
);