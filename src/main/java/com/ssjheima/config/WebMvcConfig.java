package com.ssjheima.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssjheima.controller.AuthController;
import com.ssjheima.pojo.Result;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor(objectMapper))
                .addPathPatterns(
                        "/auth/logout",
                        "/auth/me",
                        "/photos/**"
                )
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/uploads/**",
                        "/",
                        "/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.webp",
                        "/**/*.gif"
                );
    }

    static class LoginCheckInterceptor implements HandlerInterceptor {
        private final ObjectMapper objectMapper;

        LoginCheckInterceptor(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            HttpSession session = request.getSession(false);
            Integer uid = null;
            if (session != null) {
                uid = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
            }
            if (uid != null) {
                return true;
            }
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.error("NOT_LOGIN")));
            return false;
        }
    }
}

