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

package io.durg.kirtimukh.throttling.checker.impl;

import io.durg.kirtimukh.throttling.checker.StrategyChecker;
import io.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.durg.kirtimukh.throttling.window.impl.LeakyBucketWindowChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by pradeep.dalvi on 21/10/20
 */
class LeakyBucketTickerTest {

    private StrategyChecker strategyChecker;

    @BeforeEach
    void setUp() {
        strategyChecker = new LeakyBucketStrategyChecker(LeakyBucketWindowChecker.builder()
                .strategyConfig(LeakyBucketThrottlingStrategyConfig.builder()
                        .threshold(1)
                        .build())
                .build());
    }

    @AfterEach
    void tearDown() {
        strategyChecker = null;
    }

    @Test
    void enter() {
        Assertions.assertDoesNotThrow(() -> {
            strategyChecker.enter();
        });

        Assertions.assertThrows(ThrottlingException.class, () -> {
            strategyChecker.enter();
        });

        strategyChecker.exit();

        Assertions.assertDoesNotThrow(() -> {
            strategyChecker.enter();
        });
    }

    @Test
    void exit() {
        Assertions.assertDoesNotThrow(() -> {
            strategyChecker.exit();
        });

        strategyChecker.enter();
        Assertions.assertThrows(ThrottlingException.class, () -> {
            strategyChecker.enter();
        });

        strategyChecker.exit();

        Assertions.assertDoesNotThrow(() -> {
            strategyChecker.enter();
        });
    }
}