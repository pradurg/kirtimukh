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

import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingManager;
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingExceptionTranslator;
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
            System.out.println(String.format("Intercepted Function %d", count));
            try {
                System.out.println(String.format("Before Sleep for Intercepted Function %d", count));
                Thread.sleep(count * 10);
                System.out.println(String.format("After Sleep for Intercepted Function %d", count));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ((random.nextInt(10) % 2) == 1) {
                throw new RuntimeException("Checking throwable & exceptions");
            }

            return String.format("SUCCESS: %d", count);
        }

        public String normalFunction(int count) {
            System.out.println(String.format("Not Intercepted Function %d", count));
            return "Not Intercepted Function Response";
        }

        @Override
        public void run() {
            int count = random.nextInt(100);
            System.out.println("Calling " + count);
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
                        .unit(ThrottlingWindowUnit.SECOND)
                        .threshold(1)
                        .build(),
                new HashMap<>(),
                new ThrottlingExceptionTranslator<RuntimeException>() {
                    @Override
                    public RuntimeException throwable(ThrottlingException e) {
                        System.out.println("Throttling request at " + e.getCardinality());
                        return new RuntimeException("Throttling request at " + e.getCardinality());
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

        Thread.sleep(1000);
        pool.execute(someFunctionsClass);
        pool.execute(someFunctionsClass);

        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}