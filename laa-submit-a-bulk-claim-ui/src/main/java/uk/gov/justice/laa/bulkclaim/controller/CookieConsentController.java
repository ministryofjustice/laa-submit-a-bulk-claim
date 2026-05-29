package uk.gov.justice.laa.bulkclaim.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class CookieConsentController {

    @PostMapping("/cookies/consent")
    public String handleCookieConsent(
            @RequestParam("analytics") String analytics,
            HttpServletResponse response, HttpServletRequest request) {
        // Build the preference value \
        String cookieValue = "{\"analytics\":" + "yes".equals(analytics) + "}";
        Cookie consentCookie = new Cookie("cookies_policy", URLEncoder.encode(cookieValue, StandardCharsets.UTF_8));

        consentCookie.setPath("/");
        consentCookie.setHttpOnly(false);
        consentCookie.setSecure(false);
        // 1 year
        consentCookie.setMaxAge(365 * 24 * 60 * 60);

        // Must be readable by JS for GA4 consentCookie.setSecure(true);
        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/; Secure; SameSite=Lax",
                consentCookie.getName(),
                consentCookie.getValue(),
                consentCookie.getMaxAge()
        );
        response.setHeader("Set-Cookie", cookieHeader);
        // Redirect back to where the user came from
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/cookies/hide")
    public String hideCookieMessage(
            HttpServletResponse response, HttpServletRequest request) {
        Cookie hiddenCookie = new Cookie("cookies_banner_hidden", "true");
        hiddenCookie.setPath("/");
        hiddenCookie.setHttpOnly(false);
        hiddenCookie.setSecure(false);
        // 1 year
        hiddenCookie.setMaxAge(365 * 24 * 60 * 60);

        response.addCookie(hiddenCookie);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/cookies")
    public String cookiesPage(HttpServletRequest request, Model model, @RequestParam(required = false) Boolean success) {
        Boolean analyticsConsented = (Boolean) request.getAttribute("analyticsConsented");
        model.addAttribute("analyticsCookiesEnabled", analyticsConsented !=null && analyticsConsented);
        model.addAttribute("showSuccessBanner", success != null && success);

        return "pages/cookies";
    }

    @PostMapping("/cookies/preferences")
    public String hideCookieMessage(
            @RequestParam("analytics") String analytics, HttpServletResponse response) {
        String cookieValue = "{\"analytics\":" + "yes".equals(analytics) + "}";
        Cookie consentCookie = new Cookie("cookies_policy", URLEncoder.encode(cookieValue, StandardCharsets.UTF_8));

        consentCookie.setPath("/");
        consentCookie.setHttpOnly(false);
        // 1 year
        consentCookie.setMaxAge(365 * 24 * 60 * 60);

        // Must be readable by JS for GA4 consentCookie.setSecure(true);
        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/; Secure; SameSite=Lax",
                consentCookie.getName(),
                consentCookie.getValue(),
                consentCookie.getMaxAge()
        );
        response.setHeader("Set-Cookie", cookieHeader);
        // Redirect back to where the user came from
        return "redirect:/cookies?success=true";
    }
}
