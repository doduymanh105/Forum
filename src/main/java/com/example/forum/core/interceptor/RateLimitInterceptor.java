package com.example.forum.core.interceptor;

import com.example.forum.core.annotation.RateLimit;
import com.example.forum.core.exception.RateLimitExceededException;
import com.example.forum.domain.UserEntity;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final LettuceBasedProxyManager<byte[]> proxyManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!(handler instanceof HandlerMethod handlerMethod)){
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true; // Không dán nhãn -> Mở cửa tự do
        }

        String key = resolveKey(request);

        Supplier<BucketConfiguration> configSupplier = () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(rateLimit.capacity())
                        .refillGreedy(rateLimit.capacity(), Duration.of(rateLimit.time(), rateLimit.unit()))
                )
                .build();

        var bucket = proxyManager.builder()
                .build(key.getBytes(),configSupplier );

        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);

        if(consumptionProbe.isConsumed()){
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(consumptionProbe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = consumptionProbe.getNanosToWaitForRefill();
            log.warn("[RATE_LIMIT]: Forbit {} spam API {}. Try after {}s", key, request.getRequestURI(), waitForRefill);

            throw new RateLimitExceededException(waitForRefill);
        }
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")){

            UserEntity userDetails = (UserEntity) auth.getPrincipal();
            return "rate_limit:user:" + userDetails.getUserId()+ ":" + request.getRequestURI();
        } else {
            return "rate_limit:ip:" + request.getRemoteAddr() + ":" + request.getRequestURI();
        }
    }
}
