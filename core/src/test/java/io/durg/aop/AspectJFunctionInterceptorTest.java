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
import io.durg.kirtimukh.throttling.ThrottlingExceptionTranslator;
import io.durg.kirtimukh.throttling.ThrottlingManager;
import io.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.custom.ThrottlingVerdict;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.durg.kirtimukh.throttling.exception.impl.QuotaThrottlingException;
import io.durg.kirtimukh.throttling.window.impl.CustomThrottlingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by pradeep.dalvi on 14/10/20
 */
class AspectJFunctionInterceptorTest {
    Random random = new Random();

    public class SomeFunctionsClass implements Runnable {
        @Throttle
        public String rateLimitedFunction(int count) {
            System.out.printf("Intercepted Function %d\n", count);
            try {
                System.out.printf("Before Sleep for Intercepted Function %d\n", count);
                Thread.sleep(count * 10);
                System.out.printf("After Sleep for Intercepted Function %d\n", count);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ((random.nextInt(10) % 2) == 1) {
                throw new RuntimeException("Checking throwable & exceptions\n");
            }

            return String.format("SUCCESS: %d", count);
        }

        public String normalFunction(int count) {
            System.out.printf("Not Intercepted Function %d\n", count);
            return "Not Intercepted Function Response";
        }

        @Override
        public void run() {
            int count = random.nextInt(100);
            System.out.printf("Calling %d\n", count);
            if ((count % 2) != 1) {
                try {
                    String response = rateLimitedFunction(count);
                    System.out.println(response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                normalFunction(count);
            }
        }
    }

    private SomeFunctionsClass someFunctionsClass;

    @BeforeEach
    public void setup() {
        ThrottlingManager.initialise(LeakyBucketThrottlingStrategyConfig.builder()
                        .threshold(1)
                        .build(),
                new HashMap<>(),
                null,
                new ThrottlingExceptionTranslator<RuntimeException>() {
                    @Override
                    public RuntimeException throwable(ThrottlingException e) {
                        return e.getStrategyType()
                                .accept(new ThrottlingStrategyType.Visitor<RuntimeException>() {
                                    @Override
                                    public RuntimeException visitQuota() {
                                        return timed((QuotaThrottlingException) e);
                                    }

                                    @Override
                                    public RuntimeException visitLeakyBucket() {
                                        return timed((QuotaThrottlingException) e);
                                    }

                                    @Override
                                    public RuntimeException visitPriorityBucket() {
                                        return timed((QuotaThrottlingException) e);
                                    }

                                    @Override
                                    public RuntimeException visitCustomStrategy() {
                                        return ng((CustomThrottlingException) e);
                                    }
                                });
                    }

                    public RuntimeException timed(QuotaThrottlingException e) {
                        System.out.println("Throttling request at " + e.getCardinality());
                        return new RuntimeException("Throttling request at " + e.getCardinality());
                    }

                    public RuntimeException ng(CustomThrottlingException customThrottlingException) {
                        ThrottlingVerdict verdict = customThrottlingException.getVerdict();
                        return verdict
                                .accept(new ThrottlingVerdict.Visitor<RuntimeException>() {
                                    @Override
                                    public RuntimeException visitAllow() {
                                        return null;
                                    }

                                    @Override
                                    public RuntimeException visitDeny() {
                                        System.out.println("Throttling request with verdict " + verdict);
                                        return customThrottlingException;
                                    }

                                    @Override
                                    public RuntimeException visitWait() {
                                        System.out.println("Throttling request with verdict " + verdict);
                                        return customThrottlingException;
                                    }

                                    @Override
                                    public RuntimeException visitAck() {
                                        System.out.println("Throttling request with verdict " + verdict);
                                        return customThrottlingException;
                                    }
                                });
                    }
                },
                null);
        someFunctionsClass = new SomeFunctionsClass();
    }

    @Test
    void testAspectJBasic() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);

        pool.awaitTermination(3, TimeUnit.SECONDS);
    }
}