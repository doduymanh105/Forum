package com.example.forum.core.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut(){}

    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable{
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String method = request.getMethod();
        String url = request.getRequestURI();
        String ip = request.getRemoteAddr();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        log.info("[API START] {} {} - IP: {} - Handler: {}.{}()", method, url, ip, className, methodName);

        Object result;

        try {
            result = joinPoint.proceed();
        } catch (IllegalArgumentException e) {
            log.error("[API ERROR] Illegal argument in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        }
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("[API END] {} {} - Time: {}ms", method, url, executionTime);

        return result;
    }
}
