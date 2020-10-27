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

package io.kalp.athang.durg.kirtimukh.throttling;

import io.kalp.athang.aop.ThrottlingBucketKey;
import io.kalp.athang.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.QuotaThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * Created by pradeep.dalvi on 25/10/20
 */
class ThrottlingControllerTest {
    ThrottlingController throttlingController;

    @BeforeEach
    void setUp() {
        HashMap<String, ThrottlingStrategyConfig> strategyConfigs = new HashMap<>();
        strategyConfigs.put("ThrottlingControllerTest.testQuota", QuotaThrottlingStrategyConfig.builder()
                .unit(ThrottlingWindowUnit.MILLISECOND)
                .threshold(1)
                .build());
        strategyConfigs.put("ThrottlingControllerTest.testQuotaWindows", QuotaThrottlingStrategyConfig.builder()
                .unit(ThrottlingWindowUnit.SECOND)
                .threshold(1)
                .windows(1)
                .build());
        strategyConfigs.put("ThrottlingControllerTest.testPriorityBucket", QuotaThrottlingStrategyConfig.builder()
                .unit(ThrottlingWindowUnit.MINUTE)
                .threshold(1)
                .build());
        throttlingController = new ThrottlingController(LeakyBucketThrottlingStrategyConfig.builder()
                .unit(ThrottlingWindowUnit.SECOND)
                .threshold(1)
                .build(),
                strategyConfigs);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getInfo() {
        throttlingController.register(ThrottlingBucketKey.builder()
                .clazz(ThrottlingControllerTest.class)
                .functionName("getInfo")
                .build());
        Assertions.assertNotNull(throttlingController.getInfo());
    }

    @Test
    void registerDefault() {
        throttlingController.register(ThrottlingBucketKey.builder()
                .clazz(ThrottlingControllerTest.class)
                .functionName("getInfo")
                .build());
        Assertions.assertNotNull(throttlingController.getInfo());
        Assertions.assertNotNull(throttlingController.getInfo().get("ThrottlingControllerTest.getInfo"));
    }

    @Test
    void registerQuota() {
        throttlingController.register(ThrottlingBucketKey.builder()
                .clazz(ThrottlingControllerTest.class)
                .functionName("testQuota")
                .build());
        Assertions.assertNotNull(throttlingController.getInfo());
        Assertions.assertNotNull(throttlingController.getInfo().get("ThrottlingControllerTest.testQuota"));
    }

    @Test
    void registerQuotaWithWindows() {
        throttlingController.register(ThrottlingBucketKey.builder()
                .clazz(ThrottlingControllerTest.class)
                .functionName("testQuotaWindows")
                .build());
        Assertions.assertNotNull(throttlingController.getInfo());
        Assertions.assertNotNull(throttlingController.getInfo().get("ThrottlingControllerTest.testQuotaWindows"));
    }

    @Test
    void registerPriorityBucket() {
        throttlingController.register(ThrottlingBucketKey.builder()
                .clazz(ThrottlingControllerTest.class)
                .functionName("testPriorityBucket")
                .build());
        Assertions.assertNotNull(throttlingController.getInfo());
        Assertions.assertNotNull(throttlingController.getInfo().get("ThrottlingControllerTest.testPriorityBucket"));
    }
}