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

package io.kalp.athang.aop;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingManager;
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle;
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttleable;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingStage;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingExceptionTranslator;
import io.kalp.athang.durg.kirtimukh.throttling.ticker.StrategyChecker;
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
@VisibleForTesting
public class ThrottlingFunctionWrapper {
    @Pointcut("@annotation(io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle)")
    public void throttlePointcutFunction() {
        // To be empty
    }

    @Pointcut("@annotation(io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttleable)")
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
        final ThrottlingExceptionTranslator<? extends RuntimeException> translator = ThrottlingManager.getTranslator();
        try {
            checker.enter();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.ENTERED, stopwatch);
        } catch (ThrottlingException e) {
            stopwatch.stop();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.THROTTLED, stopwatch);
            if (translator != null) {
                throw translator.throwable(e);
            } else {
                throw e;
            }
        }
    }

    private Object exit(final ThrottlingBucketKey bucketKey,
                        final ProceedingJoinPoint joinPoint,
                        final StrategyChecker checker,
                        final Stopwatch stopwatch) throws Throwable {
        Object response = null;

        try {
            response = joinPoint.proceed();
            stopwatch.stop();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.COMPLETED, stopwatch);
        } catch (Exception e) {
            stopwatch.stop();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.ERROR, stopwatch);
        } finally {
            checker.exit();
            ThrottlingManager.ticker(bucketKey, ThrottlingStage.ACCEPTED, stopwatch);
        }
        return response;
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