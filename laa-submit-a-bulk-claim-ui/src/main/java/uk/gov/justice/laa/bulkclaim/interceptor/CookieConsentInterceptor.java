package uk.gov.justice.laa.bulkclaim.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class CookieConsentInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        boolean analyticsConsented = false;
        boolean bannerSeen = false;
        boolean bannerHidden = false;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("cookies_policy".equals(cookie.getName())) {
                    String val = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                    analyticsConsented = val.contains("\"analytics\":true");
                    bannerSeen = true;
                }
                if("cookies_banner_hidden".equals(cookie.getName())) {
                    bannerHidden = true;
                }
            }
        }
        request.setAttribute("analyticsConsented", analyticsConsented);
        request.setAttribute("showCookieBanner", !bannerSeen);
        request.setAttribute("bannerSeen", bannerSeen && !bannerHidden);
        return true;
    }
}