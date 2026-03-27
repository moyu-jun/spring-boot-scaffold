package com.junmoyu.security.core;

/**
 * 安全上下文（线程安全）
 * <p>
 * 使用 ThreadLocal 存储当前线程的认证信息，支持虚拟线程
 */
public final class SecurityContext {

    private static final ThreadLocal<Authentication> CONTEXT = ThreadLocal.withInitial(Authentication::empty);

    private SecurityContext() {
    }

    /**
     * 获取当前认证信息
     */
    public static Authentication getAuthentication() {
        return CONTEXT.get();
    }

    /**
     * 设置当前认证信息
     */
    public static void setAuthentication(Authentication authentication) {
        CONTEXT.set(authentication != null ? authentication : Authentication.empty());
    }

    /**
     * 清除当前认证信息
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        return getAuthentication().userId();
    }

    /**
     * 获取当前用户账号
     */
    public static String getAccount() {
        return getAuthentication().account();
    }

    /**
     * 判断当前是否已认证
     */
    public static boolean isAuthenticated() {
        return getAuthentication().isAuthenticated();
    }
}
