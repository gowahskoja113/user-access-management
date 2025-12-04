package com.r2s.auth.security;

import com.r2s.auth.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Import interface này
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Tự tạo Constructor (thay cho dòng 7-10 trong ảnh hướng dẫn)
public class JwtFilter extends OncePerRequestFilter { // <--- QUAN TRỌNG: Phải có dòng này mới hết lỗi

    private final JwtUtil jwtUtil;

    // Lưu ý: Dùng Interface UserDetailsService cho chuẩn Spring, hoặc dùng CustomUserDetailsService cũng được
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lấy header Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // 1. Kiểm tra header có chứa Bearer Token không
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Cắt bỏ chữ "Bearer "
            username = jwtUtil.extractUsername(token); // Lấy username từ token
        }

        // 2. Nếu có username và chưa đăng nhập (Context null)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Lấy thông tin user từ DB lên
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 3. Kiểm tra token có hợp lệ với user này không
            if (jwtUtil.validateToken(token, userDetails)) {

                // Tạo đối tượng xác thực
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set vào Security Context (Báo cho Spring biết "thằng này đã đăng nhập rồi")
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}