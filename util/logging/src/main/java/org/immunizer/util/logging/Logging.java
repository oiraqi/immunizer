package org.immunizer.util.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class Logging {

    @Around("@annotation(org.immunizer.util.logging.Logged)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        
        log.trace("Entering: " + joinPoint.getSignature().toLongString());

        Object proceed = joinPoint.proceed();

        log.trace("Exiting: " + joinPoint.getSignature().toLongString());

        return proceed;
    }

}