package com.bookmarks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Support OPTIONS requests without auth
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String username = JwtUtil.validateTokenAndGetSubject(token);
        if (username == null) {
            throw new UnauthorizedException("Token is invalid or expired");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        request.setAttribute("currentUser", user);
        return true;
    }
}
