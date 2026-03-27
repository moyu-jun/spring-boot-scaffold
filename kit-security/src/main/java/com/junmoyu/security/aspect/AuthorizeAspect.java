package com.junmoyu.security.aspect;

import com.junmoyu.basic.exception.AccessDeniedException;
import com.junmoyu.security.SecurityProperties;
import com.junmoyu.security.annotation.PreAuthorize;
import com.junmoyu.security.core.AuthorizeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限校验切面
 * <p>
 * 拦截 @PreAuthorize 注解，解析 SpEL 表达式并校验权限
 * <p>
 * 使用参数绑定方式，避免硬编码包名，适配脚手架场景
 */
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = SecurityProperties.SECURITY_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizeAspect {

    private final AuthorizeService authorizeService;

    /**
     * SpEL 表达式解析器（线程安全）
     */
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * SpEL 表达式缓存（避免重复解析）
     */
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * 拦截方法上的 @PreAuthorize 注解
     * 使用参数绑定，无需硬编码包名
     */
    @Before("@annotation(preAuthorize)")
    public void checkMethodAuthorize(PreAuthorize preAuthorize) {
        checkAuthorize(preAuthorize.value());
    }

    /**
     * 拦截类上的 @PreAuthorize 注解
     * 使用参数绑定，无需硬编码包名
     * 注意：方法上的注解优先级更高，会先被 checkMethodAuthorize 拦截
     */
    @Before("@within(preAuthorize)")
    public void checkClassAuthorize(JoinPoint joinPoint, PreAuthorize preAuthorize) {
        // 检查方法上是否有注解，如果有则跳过（优先级：方法 > 类）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getAnnotation(PreAuthorize.class) == null) {
            checkAuthorize(preAuthorize.value());
        }
    }

    /**
     * 校验权限表达式
     *
     * @param expressionStr SpEL 表达式
     */
    private void checkAuthorize(String expressionStr) {
        if (StringUtils.isBlank(expressionStr)) {
            throw new AccessDeniedException("权限表达式不能为空");
        }

        // 从缓存获取或解析表达式
        Expression expression = expressionCache.computeIfAbsent(
                expressionStr,
                expressionParser::parseExpression
        );

        // 创建评估上下文
        StandardEvaluationContext context = new StandardEvaluationContext(authorizeService);

        // 执行表达式并获取结果
        Boolean result = expression.getValue(context, Boolean.class);

        if (!Boolean.TRUE.equals(result)) {
            throw new AccessDeniedException();
        }
    }
}
