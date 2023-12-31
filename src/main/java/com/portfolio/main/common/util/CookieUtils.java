package com.portfolio.main.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieUtils {
    public static String getCookie(HttpServletRequest request, String cookieName) {
        String value = null;
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    value = cookie.getValue();
                    break;
                }
            }
        }
        return value;
    }

    public static void setCookie(HttpServletResponse response, Cookie cookie) {
        response.addCookie(cookie);
    }

    public static boolean isExpired(Cookie cookie) {
        return cookie.getMaxAge() <= 0;
    }
}
