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

import com.google.common.base.Stopwatch;
import io.durg.aop.annotation.Throttle;
import io.durg.aop.annotation.Throttleable;
import io.durg.kirtimukh.throttling.ThrottlingBucketKey;
import io.durg.kirtimukh.throttling.ThrottlingManager;
import io.durg.kirtimukh.throttling.enums.ThrottlingStage;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.durg.kirtimukh.throttling.ticker.StrategyChecker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Objects;

/**
 * Created by pradeep.dalvi on 14/10/20
 */
@Aspect
public class ThrottlingFunctionWrapper {
    @Pointcut("@annotation(io.durg.aop.annotation.Throttle)")
    public void throttlePointcutFunction() {
        // To be empty
    }

    @Pointcut("@annotation(io.durg.aop.annotation.Throttleable)")
    public void throttleablePointcutFunction() {
        // To be empty
    }

    @Pointcut("execution(* *(..))")
    public void pointCutExecution() {
        // To be empty
    }

    private ThrottlingBucketKey getThrottleBucketKey(final Signature signature) {
        MethodSignature methodSignature = MethodSignature.class.cast(signature);
        final Throttle throttleFunction = methodSignature.getMethod()
                .getAnnotation(Throttle.class);
        String bucketName = null;
        if (Objects.isNull(throttleFunction)) {
            final Throttleable throttleableFunction = methodSignature.getMethod()
                    .getAnnotation(Throttleable.class);
            if (Objects.isNull(throttleableFunction)) {
                throw new UnsupportedOperationException("Pointcut called without annotations");
            }

            bucketName = throttleableFunction.bucket();
        } else {
            bucketName = throttleFunction.bucket();
        }

        return ThrottlingBucketKey.builder()
                .bucketName(bucketName)
                .clazz(methodSignature.getDeclaringType())
                .functionName(methodSignature.getMethod().getName())
                .build();
    }

    private StrategyChecker getStrategyChecker(final ThrottlingBucketKey bucketKey) {
        return ThrottlingManager.register(bucketKey);
    }

    private void enter(final ThrottlingBucketKey bucketKey,
                       final StrategyChecker checker,
                       final Stopwatch stopwatch) {
        try {
            checker.enter();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.ENTERED, stopwatch);
        } catch (ThrottlingException e) {
            stopwatch.stop();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.THROTTLED, stopwatch);
            ThrottlingManager.translate(e);
        }
    }

    private Object exit(final ThrottlingBucketKey bucketKey,
                        final ProceedingJoinPoint joinPoint,
                        final StrategyChecker checker,
                        final Stopwatch stopwatch) throws Throwable {
        try {
            Object response = joinPoint.proceed();
            stopwatch.stop();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.COMPLETED, stopwatch);
            return response;
        } catch (Throwable t) {
            stopwatch.stop();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.ERROR, stopwatch);
            throw t;
        } finally {
            checker.exit();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.ACCEPTED, stopwatch);
        }
    }

    @Around("(throttlePointcutFunction() || throttleablePointcutFunction()) && pointCutExecution()")
    public Object processThrottle(final ProceedingJoinPoint joinPoint) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ThrottlingBucketKey bucketKey = getThrottleBucketKey(joinPoint.getSignature());

        StrategyChecker checker = getStrategyChecker(bucketKey);

        enter(bucketKey, checker, stopwatch);

        return exit(bucketKey, joinPoint, checker, stopwatch);
    }
}