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

package io.durg.kirtimukh.throttling;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import io.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingStage;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by pradeep.dalvi on 25/10/20
 */
class ThrottlingManagerTest {

    @BeforeEach
    void setUp() {
        ThrottlingManager.initialise(LeakyBucketThrottlingStrategyConfig.builder()
                        .unit(ThrottlingWindowUnit.SECOND)
                        .threshold(1)
                        .build(),
                new HashMap<>(),
                null,
                e -> new UnsupportedOperationException(),
                new MetricRegistry());
    }

    @Test
    void getInfo() {
        Assertions.assertNotNull(ThrottlingManager.getInfo());
    }

    @Test
    void register() {
        Assertions.assertNotNull(ThrottlingManager.register(ThrottlingKey.builder()
                .clazz(ThrottlingManagerTest.class)
                .functionName("register")
                .build()));
    }

    @Test
    void tickerWithoutBucket() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ThrottlingManager.ticker(ThrottlingKey.builder()
                        .clazz(ThrottlingManagerTest.class)
                        .functionName("ticker")
                        .build(),
                ThrottlingStage.ENTERED,
                stopwatch);
        Assertions.assertTrue(stopwatch.elapsed(TimeUnit.NANOSECONDS) > 0);
    }

    @Test
    void tickerWithBucket() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ThrottlingManager.ticker(ThrottlingKey.builder()
                        .bucketName("TEST_BUCKET")
                        .clazz(ThrottlingManagerTest.class)
                        .functionName("ticker")
                        .build(),
                ThrottlingStage.THROTTLED,
                stopwatch);
        Assertions.assertTrue(stopwatch.elapsed(TimeUnit.NANOSECONDS) > 0);
    }
}