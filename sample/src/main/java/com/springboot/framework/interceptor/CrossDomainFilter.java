package com.springboot.framework.interceptor;

import com.springboot.framework.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 允许跨域过滤器
 * Filter比Spring的Interceptor更早介入请求生命周期，所以可以更早的处理OPTIONS请求.
 *
 * @author LJH
 */
public class CrossDomainFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CrossDomainFilter.class);

    private volatile boolean allowCrossDomain = true;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 设置允许跨域访问
        LOG.debug("Request Orign = {},url={}", request.getHeader("Origin"), request.getRequestURL());

        if (allowCrossDomain) {
            // 重要：clientIp不能为*，否则session无法传递到服务器端.
            response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.addHeader("Access-Control-Allow-Credentials", "true");

            /*
             * 处理 Preflight 情况下的额外返回数据:
             * https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS#
             * Preflighted_requests 需要确认 Preflight 是有效的请求，而不是直接进行的OPTIONS操作.
             */
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
            response.addHeader("Access-Control-Allow-Headers",
                    "X-Requested-With, X-Access-Token, " + Const.ACCESS_TOKEN_HEADER_NAME +
                            ", X-Upload-Auth-Token, Origin, Content-Type, Cookie," + Const.REQUEST_SIDE_HEAD_NAME);
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        // 浏览器是会先发一次options请求，如果请求通过，则继续发送正式的post请求
        // 如果是option请求，直接返回200
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        // 传递业务请求处理
        filterChain.doFilter(request, response);
    }

    public void setAllowCrossDomain(boolean allowCrossDomain) {
        this.allowCrossDomain = allowCrossDomain;
    }
}
