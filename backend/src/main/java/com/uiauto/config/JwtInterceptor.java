package com.uiauto.config;

import com.uiauto.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String REDIS_TOKEN_PREFIX = "user:token:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行登录接口和无需认证的接口
        String uri = request.getRequestURI();
        if (uri.contains("login") || uri.startsWith("/api/case") || uri.startsWith("/api/execution") || uri.startsWith("/api/testcase") || uri.equals("/api/auth/refresh") || uri.contains("reset-password")) {
            return true;
        }

        // 获取 token
        String token = request.getHeader(TOKEN_HEADER);
        if (!StringUtils.hasText(token) || !token.startsWith(TOKEN_PREFIX)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或 token 无效\"}");
            return false;
        }

        token = token.substring(TOKEN_PREFIX.length());

        // 尝试从 token 中获取 userId（支持 mock-token 和真实 JWT）
        Long userId = null;
        
        // 先尝试 JWT 解析
        userId = jwtUtil.validateToken(token);
        
        // 如果 JWT 解析失败，尝试从 mock-token 中解析（格式：mock-token-{timestamp}）
        if (userId == null && token.startsWith("mock-token-")) {
            // mock-token 格式：mock-token-{userId}-{timestamp}
            // 从 Redis 中查找
            try {
                // 尝试从 Redis 获取 userId（登录时应该已存储）
                // 或者从 token 格式中解析（如果包含 userId）
                // 这里简单处理：mock-token 格式的用户 ID 从 token 中无法直接获取
                // 所以需要修改登录逻辑或者 mock-token 格式
                
                // 临时方案：从请求参数或默认用户获取
                // 更好的方案是使用真实的 JWT
            } catch (Exception e) {
                // ignore
            }
        }

        // 如果无法从 token 获取 userId，返回 401
        if (userId == null) {
            // 兼容处理：如果是 mock-token，尝试从 Redis 存储的 token 映射中获取
            Object cachedUserId = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + token);
            if (cachedUserId != null) {
                userId = Long.parseLong(String.valueOf(cachedUserId));
            }
        }

        if (userId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token 已过期\"}");
            return false;
        }

        // 检查 Redis 中是否存在 token（如果需要强制在线）
        // Object cachedUserId = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + token);
        // if (cachedUserId == null) {
        //     response.setStatus(401);
        //     response.setContentType("application/json;charset=UTF-8");
        //     response.getWriter().write("{\"code\":401,\"message\":\"token 已失效\"}");
        //     return false;
        // }

        // 将 userId 存入 request 属性
        request.setAttribute("userId", userId);
        return true;
    }
}
