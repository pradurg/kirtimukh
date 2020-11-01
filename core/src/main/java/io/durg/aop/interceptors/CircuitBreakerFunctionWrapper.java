/*
 * Copyright (c) 2020 Pradeep A. Dalvi <prad@apache.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.durg.aop.interceptors;

import com.google.common.base.Strings;
import io.durg.aop.annotation.CircuitBreaker;
import io.durg.kirtimukh.circuitbreaker.CircuitBreakerKey;
import io.durg.kirtimukh.circuitbreaker.hystrix.HystrixCommandController;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by pradeep.dalvi on 27/10/20
 */
@Aspect
public class CircuitBreakerFunctionWrapper {
    @Pointcut("@annotation(io.durg.aop.annotation.CircuitBreaker)")
    public void circuitBreakerPointcutFunction() {
        // To be empty
    }

    @Pointcut("execution(* *(..))")
    public void pointCutExecution() {
        // To be empty
    }

    private CircuitBreakerKey getCircuitBreakerKey(final Signature signature) {
        MethodSignature methodSignature = MethodSignature.class.cast(signature);
        final CircuitBreaker circuitBreaker = methodSignature.getMethod()
                .getAnnotation(CircuitBreaker.class);

        return CircuitBreakerKey.builder()
                .domain(circuitBreaker.domain())
                .group(Strings.isNullOrEmpty(circuitBreaker.group())
                        ? methodSignature.getDeclaringType().getSimpleName()
                        : circuitBreaker.group())
                .command(Strings.isNullOrEmpty(circuitBreaker.command())
                        ? methodSignature.getMethod().getName()
                        : circuitBreaker.command())
                .build();
    }

    @Around("circuitBreakerPointcutFunction() && pointCutExecution()")
    public Object processCircuitBreaker(final ProceedingJoinPoint joinPoint) throws Throwable {
        return HystrixCommandController.execute(getCircuitBreakerKey(joinPoint.getSignature()), joinPoint);
    }
}
