package net.lightoze.gwt.i18n.server;

import org.apache.commons.lang.LocaleUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class I18nFilter implements Filter {
    static {
        LocaleProxy.initialize();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        Locale locale = req.getMethod().equals("GET") ? LocaleUtils.toLocale(servletRequest.getParameter("locale")) : null;
        if (locale != null) {
            req.getSession(true).setAttribute("locale", locale);
        } else {
            HttpSession session = req.getSession(false);
            if (session != null) {
                locale = (Locale) session.getAttribute("locale");
            }
        }

        // if locale still not set, try to get it from cookies
        if (locale == null) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie curr : cookies) {
                    if (curr.getName().equals("locale")) {
                        locale = LocaleUtils.toLocale(curr.getValue());
                        break;
                    }
                }
            }
        }

        if (locale == null) {
            locale = servletRequest.getLocale();
        }

        ThreadLocalLocaleProvider.pushLocale(locale);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            ThreadLocalLocaleProvider.popLocale();
        }
    }

    @Override
    public void destroy() {
    }
}
