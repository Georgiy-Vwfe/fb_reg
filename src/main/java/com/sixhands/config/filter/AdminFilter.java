package com.sixhands.config.filter;

import org.apache.logging.log4j.util.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@Order(1)
public class AdminFilter implements Filter {
    private String correctToken;
    public AdminFilter(String correctToken){
        this.correctToken = correctToken;
    }

    @Override public void init(FilterConfig filterConfig) throws ServletException {}
    @Override public void destroy() {}
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        URL url = new URL( req.getRequestURL()+"" );
        // '','admin','*token*',...
        String[] splitPath = url.getPath().split("/");
        if(splitPath.length < 3) { resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Token is not valid"); return; }

        String token = splitPath[2];
        System.out.println(token+", correct: "+correctToken);
        if(StringUtils.isEmpty(token) || !token.equals(correctToken)){ resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Token is not valid"); return; }

        chain.doFilter(servletRequest, servletResponse);
    }
}
