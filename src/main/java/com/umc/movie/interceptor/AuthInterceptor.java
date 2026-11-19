package com.umc.movie.interceptor;

import com.umc.movie.exception.ApiException;
import com.umc.movie.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String MEMBER_ID = "memberId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        // CORS preflight 요청은 토큰 없이 통과
        String method = request.getMethod();
        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        String header =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        // 헤더가 없거나 Bearer 형식이 아니면 401
        if (header == null
                || !header.startsWith(BEARER_PREFIX)) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTH401_2",
                    "로그인이 필요한 요청입니다."
            );
        }

        // "Bearer " 뒤의 토큰만 잘라서 검증
        String token =
                header.substring(BEARER_PREFIX.length());
        Long memberId = jwtUtil.getMemberId(token);

        // 컨트롤러에서 꺼내 쓸 수 있게 저장
        request.setAttribute(MEMBER_ID, memberId);
        return true;
    }
}