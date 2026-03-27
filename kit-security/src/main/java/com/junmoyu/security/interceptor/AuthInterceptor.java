package com.junmoyu.security.interceptor;

import com.junmoyu.basic.exception.AccessDeniedException;
import com.junmoyu.security.SecurityProperties;
import com.junmoyu.security.core.Authentication;
import com.junmoyu.security.core.SecurityContext;
import com.junmoyu.security.core.UserDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * <p>
 * 从请求头中提取 Token 并进行认证，将认证信息存入 SecurityContext
 */
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserDetail userDetail;
    private final SecurityProperties securityProperties;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        SecurityProperties.TokenConfig tokenConfig = securityProperties.getToken();
        String authHeader = request.getHeader(tokenConfig.getHeaderName());

        // 检查 Authorization 头是否存在且格式正确
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(tokenConfig.getPrefix())) {
            log.warn("请求缺少有效的 Authorization 头: {}", request.getRequestURI());
            throw new AccessDeniedException("缺少认证信息");
        }

        // 提取 Token
        String token = authHeader.substring(tokenConfig.getPrefix().length());

        // 执行认证
        Authentication authentication = userDetail.authentication(token);
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Token 认证失败: {}", request.getRequestURI());
            throw new AccessDeniedException("认证失败");
        }

        // 将认证信息存入上下文
        SecurityContext.setAuthentication(authentication);
        log.debug("用户认证成功: userId={}, account={}", authentication.userId(), authentication.account());

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        // 清理上下文，防止内存泄漏（尤其是虚拟线程场景）
        SecurityContext.clear();
    }
}
