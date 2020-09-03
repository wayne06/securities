package com.wnzhong.counter.filter;

import com.google.common.collect.Sets;
import com.wnzhong.counter.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Component
public class SessionCheckFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Autowired
    private AccountService accountService;

    private Set<String> whiteRootPaths = Sets.newHashSet("login", "msgsocket", "test");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // 解决 ajax 跨域问题
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        String[] uriElements = uri.split("/");
        if (uriElements.length < 2) {
            request.getRequestDispatcher("/login/loginfail").forward(servletRequest, servletResponse);
        } else {
            if (!whiteRootPaths.contains(uriElements[1])) {
                if (accountService.accountExistInCache(request.getParameter("token"))) {
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    request.getRequestDispatcher("/login/loginfail").forward(servletRequest, servletResponse);
                }
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
    }

    @Override
    public void destroy() {

    }
}
