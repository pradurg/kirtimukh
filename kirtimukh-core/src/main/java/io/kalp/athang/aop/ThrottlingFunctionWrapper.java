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

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingManager;
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle;
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttleable;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingStage;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingExceptionTranslator;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.StrategyChecker;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by pradeep.dalvi on 14/10/20
 */
@Slf4j
@Aspect
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

    private String getCommandName(Signature signature) {
        MethodSignature methodSignature = MethodSignature.class.cast(signature);
        final Throttle rateLimited = methodSignature.getMethod()
                .getAnnotation(Throttle.class);

        if (Strings.isNullOrEmpty(rateLimited.name())) {
            return methodSignature.getDeclaringType().getSimpleName()
                    + '.' + methodSignature.getMethod().getName();
        } else {
            return rateLimited.name();
        }
    }

    public StrategyChecker getStrategyChecker(final String commandName) {
        return ThrottlingManager.register(commandName);
    }

    public StrategyChecker getStrategyChecker(final Throttleable throttleable, final String commandName) {
        return ThrottlingManager.register(throttleable, commandName);
    }

    private void enter(final String commandName,
                       final StrategyChecker checker,
                       final Stopwatch stopwatch) throws RuntimeException {
        final ThrottlingExceptionTranslator translator = ThrottlingManager.getTranslator();
        try {
            checker.enter();
            ThrottlingManager.ticker(commandName, ThrottlingStage.ENTERED, stopwatch);
        } catch (ThrottlingException e) {
            stopwatch.stop();
            ThrottlingManager.ticker(commandName, ThrottlingStage.THROTTLED, stopwatch);
            if (translator != null) {
                throw translator.throwable(e);
            } else {
                throw e;
            }
        }
    }

    private Object exit(final String commandName,
                        final ProceedingJoinPoint joinPoint,
                        final StrategyChecker checker,
                        final Stopwatch stopwatch) throws Throwable {
        Object response = null;

        try {
            response = joinPoint.proceed();
            stopwatch.stop();
            ThrottlingManager.ticker(commandName, ThrottlingStage.COMPLETED, stopwatch);
        } catch (Exception e) {
            stopwatch.stop();
            ThrottlingManager.ticker(commandName, ThrottlingStage.ERROR, stopwatch);
        } finally {
            checker.exit();
            ThrottlingManager.ticker(commandName, ThrottlingStage.ACCEPTED, stopwatch);
        }
        return response;
    }

    @Around("throttlePointcutFunction() && pointCutExecution()")
    public Object process(final ProceedingJoinPoint joinPoint) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String commandName = getCommandName(joinPoint.getSignature());

        StrategyChecker checker = getStrategyChecker(commandName);

        enter(commandName, checker, stopwatch);

        return exit(commandName, joinPoint, checker, stopwatch);
    }
}