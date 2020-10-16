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

import com.google.common.base.Strings;
import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingController;
import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingManager;
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingExceptionTranslator;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.StrategyChecker;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
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
    public void pointCutFunction() {
        // To be empty
    }

    @Pointcut("execution(* *(..))")
    public void pointCutExecution() {
        // To be empty
    }

    public StrategyChecker getStrategyChecker(final JoinPoint joinPoint) {
        final MethodSignature methodSignature = MethodSignature.class.cast(joinPoint.getSignature());
        final Throttle rateLimited = methodSignature.getMethod()
                .getAnnotation(Throttle.class);

        if (rateLimited == null) {
            throw new UnsupportedOperationException("Not an interceptedFunction");
        }

        final ThrottlingController controller = ThrottlingManager.getController();
        StrategyChecker checker = null;
        if (Strings.isNullOrEmpty(rateLimited.name())) {
            checker = controller.register(methodSignature.getDeclaringType().getSimpleName()
                    + '.' + methodSignature.getMethod().getName());
        } else {
            checker = controller.register(rateLimited);
        }
        return checker;
    }


    @Around("pointCutFunction() && pointCutExecution()")
    public Object process(final ProceedingJoinPoint joinPoint) throws Throwable {
        StrategyChecker checker = getStrategyChecker(joinPoint);

        final ThrottlingExceptionTranslator translator = ThrottlingManager.getTranslator();
        if (translator != null) {
            try {
                checker.enter();
            } catch (ThrottlingException e) {
                throw translator.throwable(e);
            }
        } else {
            checker.enter();
        }

        Object response = null;

        try {
            response = joinPoint.proceed();
        } finally {
            checker.exit();
        }
        return response;
    }
}