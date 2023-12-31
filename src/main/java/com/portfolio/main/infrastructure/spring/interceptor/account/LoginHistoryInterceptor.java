package com.portfolio.main.infrastructure.spring.interceptor.account;

import com.portfolio.main.application.login.dto.UserDto;
import com.portfolio.main.domain.model.account.User;
import com.portfolio.main.domain.model.account.UserLoginHistory;
import com.portfolio.main.domain.repository.account.UserLoginHistoryRepository;
import com.portfolio.main.domain.model.account.type.LoginActionType;
import com.portfolio.main.infrastructure.config.security.MyUserDetails;
import com.portfolio.main.infrastructure.config.security.jwt.JwtAuthenticationToken;
import com.portfolio.main.infrastructure.config.security.jwt.provider.JwtAuthenticationProvider;
import com.portfolio.main.infrastructure.config.security.jwt.util.TokenUtil;
import com.portfolio.main.common.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@AllArgsConstructor
@Slf4j
public class LoginHistoryInterceptor implements HandlerInterceptor {

    private JwtAuthenticationProvider jwtAuthenticationProvider;
    private UserLoginHistoryRepository userLoginHistoryRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        final String requestURI = request.getRequestURI();
        String token = getToekn(request);

        final UserDto userDto = getUser(token);

        if ("/account/login".equals(requestURI)) {
            saveLoginHistory(request, userDto);
        } else {
            saveLogoutHistory(request, userDto);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private String getToekn(HttpServletRequest request) {
        final String requestURI = request.getRequestURI();

        if("/account/login".equals(requestURI)){
            //최초 로그인시에는 응답이 나가기 전 까지 cookie에 토큰이 없으니 로그인시 Controller에서 담아준 token사용.
            return (String) request.getAttribute("token");
        } else {
            return TokenUtil.getTokenFromRequest(request);
        }
    }

    private UserDto getUser(String token) {
        final JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(token);
        final JwtAuthenticationToken authenticate = (JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(jwtAuthenticationToken);
        final MyUserDetails userDetails = (MyUserDetails) authenticate.getPrincipal();
        return userDetails.getUser();
    }

    private void saveLoginHistory(HttpServletRequest request, UserDto userDto) {
        final UserLoginHistory userLoginHistory = new UserLoginHistory(userDto.getLoginId(), LoginActionType.LOGIN, RequestUtils.getClientIP(request), RequestUtils.getClientDeviceInfo(request));
        userLoginHistoryRepository.save(userLoginHistory);
    }

    private void saveLogoutHistory(HttpServletRequest request, UserDto userDto) {
        final UserLoginHistory userLoginHistory = new UserLoginHistory(userDto.getLoginId(), LoginActionType.LOGOUT, RequestUtils.getClientIP(request), RequestUtils.getClientDeviceInfo(request));
        userLoginHistoryRepository.save(userLoginHistory);
    }
}
