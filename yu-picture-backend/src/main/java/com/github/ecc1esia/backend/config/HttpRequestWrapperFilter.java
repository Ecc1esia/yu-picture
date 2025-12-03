package com.github.ecc1esia.backend.config;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Order(1)
@Component
public class HttpRequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;

            String contentType = servletRequest.getHeader(Header.CONTENT_TYPE.getValue());

            if (ContentType.JSON.getValue().equals(contentType)) {
                chain.doFilter(new RequestWrapper(servletRequest), response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }
}
