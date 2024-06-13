package com.sparta.wildcard_newsfeed.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//@Slf4j(topic = "UseTimeAop")
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

//    private static final Logger log = LoggerFactory.getLogger(ApiLogAspect.class);


    @Before("execution(* org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.*(..))")
    public void useruser() {
//        log.info("asdfasfasfasf");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
//        log.info("URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
    }
}
