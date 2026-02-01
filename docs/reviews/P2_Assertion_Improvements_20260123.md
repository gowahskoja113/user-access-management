# [P2] Cải thiện Assertions - Hướng dẫn chi tiết

## 📋 Tổng quan

Mục tiêu: Làm cho assertions trong test **ổn định hơn**, **rõ ràng hơn**, và **phát hiện lỗi tốt hơn**.

---

## 🔴 Vấn đề 1: Dùng `assert` statement thay vì `assertEquals`

### ❌ Code hiện tại (SAI)

**File:** `user-service/src/test/java/com/r2s/user/UserIntegrationTest.java`

```java
// Dòng 62
assert savedUser.getEmail().equals("new@gmail.com");

// Dòng 160-161
assertTrue(updated.getName().equals("New Name"));
assertTrue(updated.getEmail().equals("new@gmail.com"));
```

### ⚠️ Tại sao không tốt?

1. **`assert` statement có thể bị tắt:**
   - Java assertions chỉ chạy khi JVM khởi động với flag `-ea` (enable assertions)
   - Nếu chạy test không có flag này → assertion bị bỏ qua → test pass dù logic sai
   - JUnit/Spring Boot Test mặc định **KHÔNG** enable assertions

2. **Message lỗi không rõ ràng:**
   - Khi `assert` fail: chỉ thấy `AssertionError` generic
   - Không biết giá trị expected vs actual là gì

3. **`assertTrue(x.equals(y))` không tối ưu:**
   - Nếu `x` là `null` → `NullPointerException` thay vì assertion fail
   - Message lỗi: `expected: <true> but was: <false>` → không biết giá trị thực tế

### ✅ Code nên sửa (ĐÚNG)

```java
// Dùng assertEquals - Luôn chạy, message rõ ràng
assertEquals("new@gmail.com", savedUser.getEmail());

// Hoặc dùng AssertJ (đã import sẵn trong AuthIntegrationTest)
assertThat(savedUser.getEmail()).isEqualTo("new@gmail.com");
```

### 📝 Các file cần sửa

1. **`user-service/src/test/java/com/r2s/user/UserIntegrationTest.java`**
   - Dòng 62: `assert savedUser.getEmail().equals(...)` → `assertEquals(...)`
   - Dòng 160-161: `assertTrue(...equals(...))` → `assertEquals(...)`

---

## 🔴 Vấn đề 2: Assert list chỉ check `.exists()` thay vì giá trị cụ thể

### ❌ Code hiện tại (CHƯA ĐỦ)

**File:** `user-service/src/test/java/com/r2s/user/UserIntegrationTest.java` (dòng 112-115)

```java
mockMvc.perform(get("/api/users"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$", hasSize(2)))
    .andExpect(jsonPath("$[0].username").exists()); // ❌ Chỉ check tồn tại
```

### ⚠️ Tại sao không tốt?

1. **Không phát hiện lỗi mapping:**
   - Nếu `UserMapper` map sai (ví dụ: `email` → `username`), test vẫn pass
   - Chỉ biết có field `username`, không biết giá trị đúng hay sai

2. **Không đảm bảo dữ liệu đúng:**
   - Test seed 2 users với `username="u1"`, `email="e1"`, `role=ROLE_USER`
   - Nhưng chỉ check field tồn tại → không biết có đúng giá trị không

3. **Test không tự mô tả:**
   - Đọc test không biết API trả về gì, chỉ biết "có 2 items và item đầu có username"

### ✅ Code nên sửa (ĐÚNG)

```java
mockMvc.perform(get("/api/users"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$", hasSize(2)))
    // Assert đầy đủ các field quan trọng cho item đầu
    .andExpect(jsonPath("$[0].username").value("u1"))
    .andExpect(jsonPath("$[0].email").value("e1"))
    .andExpect(jsonPath("$[0].role").value("ROLE_USER"))
    .andExpect(jsonPath("$[0].name").value("n1"))
    // Assert item thứ 2
    .andExpect(jsonPath("$[1].username").value("u2"))
    .andExpect(jsonPath("$[1].email").value("e2"))
    .andExpect(jsonPath("$[1].role").value("ROLE_USER"))
    .andExpect(jsonPath("$[1].name").value("n2"));
```

### 📝 Các file cần sửa

1. **`user-service/src/test/java/com/r2s/user/UserIntegrationTest.java`**
   - Dòng 112-115: Thêm assertions cho `email`, `role`, `name` của cả 2 items

---

## 🔴 Vấn đề 3: Assert JWT token chưa đầy đủ

### ✅ Code tốt (ĐÃ CÓ)

**File:** `auth-service/src/test/java/com/r2s/auth/AuthIntegrationTest.java` (dòng 76-104)

```java
@Test
void login_returnsJwtToken_whenCredentialsValid() throws Exception {
    // ... register và login ...
    
    MvcResult result = mockMvc.perform(...)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andReturn();
    
    // ✅ TỐT - Verify token format
    String token = objectMapper
        .readTree(result.getResponse().getContentAsString())
        .get("token")
        .asText();
    
    assertThat(token.split("\\.").length).isEqualTo(3);
}
```

### ❌ Code chưa đủ (CẦN SỬA)

**File:** `auth-service/src/test/java/com/r2s/auth/AuthIntegrationTest.java` (dòng 122-137)

```java
@Test
void login_returns200_andToken_whenCredentialsValid() throws Exception {
    // ... register và login ...
    
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists()); // ❌ Chỉ check tồn tại
}
```

### ⚠️ Tại sao cần sửa?

1. **Không đảm bảo token hợp lệ:**
   - Chỉ biết có field `token`, không biết format đúng hay sai
   - Nếu code generate token sai format (ví dụ: chỉ là string thường), test vẫn pass

2. **Không nhất quán:**
   - Test khác (dòng 76-104) đã assert format → test này cũng nên làm tương tự

3. **Không check token non-empty:**
   - Nếu token là chuỗi rỗng `""`, test vẫn pass

### ✅ Code nên sửa (ĐÚNG)

```java
@Test
void login_returns200_andToken_whenCredentialsValid() throws Exception {
    // Register
    RegisterRequest req = new RegisterRequest("loginuser", "123", "l@test.com", "L", null);
    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)));

    // Login
    LoginRequest loginReq = new LoginRequest("loginuser", "123");
    
    MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginReq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andReturn();
    
    // ✅ Assert token format đầy đủ
    String token = objectMapper
        .readTree(result.getResponse().getContentAsString())
        .get("token")
        .asText();
    
    // Assert token không rỗng
    assertThat(token).isNotEmpty();
    // Assert format JWT: header.payload.signature (3 phần)
    assertThat(token.split("\\.").length).isEqualTo(3);
}
```

### 📝 Các file cần sửa

1. **`auth-service/src/test/java/com/r2s/auth/AuthIntegrationTest.java`**
   - Dòng 122-137: Thêm assertions cho token format (non-empty + 3 parts)

---

## 📊 Tóm tắt thay đổi

| File | Dòng | Vấn đề | Cách sửa |
|------|------|--------|----------|
| `user-service/.../UserIntegrationTest.java` | 62 | `assert ...equals()` | → `assertEquals()` |
| `user-service/.../UserIntegrationTest.java` | 160-161 | `assertTrue(...equals())` | → `assertEquals()` |
| `user-service/.../UserIntegrationTest.java` | 112-115 | List chỉ check `.exists()` | → Assert `email`, `role`, `name` |
| `auth-service/.../AuthIntegrationTest.java` | 122-137 | Token chỉ check `.exists()` | → Assert non-empty + format 3 parts |

---

## 🎯 Lợi ích sau khi sửa

1. **Ổn định hơn:**
   - Assertions luôn chạy (không phụ thuộc JVM flags)
   - Phát hiện lỗi ngay cả khi code có bug nhỏ

2. **Rõ ràng hơn:**
   - Message lỗi cho biết expected vs actual
   - Test tự mô tả rõ ràng hơn (biết API trả về gì)

3. **Phát hiện lỗi tốt hơn:**
   - Phát hiện lỗi mapping/transform sớm
   - Đảm bảo dữ liệu trả về đúng format và giá trị
