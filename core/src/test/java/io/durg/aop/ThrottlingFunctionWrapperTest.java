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

package io.durg.aop;

import io.durg.aop.annotation.Throttle;
import io.durg.aop.interceptors.ThrottlingFunctionWrapper;
import io.durg.kirtimukh.throttling.ThrottlingManager;
import io.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by pradeep.dalvi on 24/10/20
 */
class ThrottlingFunctionWrapperTest {
    io.durg.aop.interceptors.ThrottlingFunctionWrapper functionWrapper;
    ProceedingJoinPoint joinPoint;
    Signature signature;

    @BeforeEach
    void setUp() {
        functionWrapper = new ThrottlingFunctionWrapper();
        joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        ThrottlingManager.initialise(LeakyBucketThrottlingStrategyConfig.builder()
                        .unit(ThrottlingWindowUnit.SECOND)
                        .threshold(1)
                        .build(),
                new HashMap<>(),
                null,
                e -> new UnsupportedOperationException(),
                null);

        Mockito.when(joinPoint.getTarget())
                .thenReturn(new ThrottlingFunctionWrapperTest());
        Mockito.when(joinPoint.getSignature())
                .thenReturn(signature);
        Mockito.when(signature.getDeclaringType())
                .thenReturn(ThrottlingFunctionWrapperTest.class);
        Mockito.when(signature.getMethod())
                .thenReturn(wrapperMethod());
    }

    public Method wrapperMethod() {
        try {
            return getClass().getDeclaredMethod("throttleMethod");
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("Not allowed");
        }
    }

    @Throttle
    public void throttleMethod() {
        // Do nothing
    }

    @Test
    void getStrategyChecker() {
        Assertions.assertDoesNotThrow(() -> {
            functionWrapper.processThrottle(joinPoint);
        });
    }
}