package com.junmoyu.iam.controller;

import com.junmoyu.basic.constant.BasicConst;
import com.junmoyu.basic.model.R;
import com.junmoyu.basic.util.HttpUtils;
import com.junmoyu.iam.model.request.LoginPasswordRequest;
import com.junmoyu.iam.model.response.TokenResponse;
import com.junmoyu.iam.service.AuthService;
import com.junmoyu.security.annotation.PreAuthorize;
import com.junmoyu.security.core.SecurityContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("login/password")
    public R<TokenResponse> loginPassword(@RequestBody LoginPasswordRequest request, HttpServletRequest httpServletRequest) {
        String ip = HttpUtils.getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");
        TokenResponse response = authService.loginPassword(request.getAccount(), request.getPassword(), ip, userAgent);
        return R.success(response);
    }

    @PostMapping("logout")
    @PreAuthorize("isAuthenticated()")
    public R<Void> logout(@RequestHeader(BasicConst.HEADER_AUTHORIZATION) String authorization) {
        String accessToken = authorization.replace(BasicConst.TOKEN_PREFIX, "");
        authService.logout(accessToken);
        return R.success();
    }

    @PostMapping("refresh")
    public R<TokenResponse> refresh(@RequestHeader(BasicConst.HEADER_AUTHORIZATION) String authorization,
                                    HttpServletRequest httpServletRequest) {
        String refreshToken = authorization.replace(BasicConst.TOKEN_PREFIX, "");
        String ip = HttpUtils.getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");
        TokenResponse response = authService.refresh(refreshToken, ip, userAgent);
        return R.success(response);
    }

    @GetMapping("me")
    @PreAuthorize("isAuthenticated()")
    public R<Object> getCurrentUser() {
        return R.success(SecurityContext.getAuthentication());
    }
}
