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

package io.durg.kirtimukh.throttling.window.impl;

import io.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.durg.kirtimukh.throttling.tick.Tick;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by pradeep.dalvi on 21/10/20
 */
class TimedWindowCheckerTest {

    private TimedWindowChecker windowChecker;

    @BeforeEach
    void setUp() {
        windowChecker = TimedWindowChecker.builder()
                .commandKey("test")
                .strategyConfig(LeakyBucketThrottlingStrategyConfig.builder()
                        .unit(ThrottlingWindowUnit.SECOND)
                        .threshold(2)
                        .build())
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void release() {
        Tick tick = windowChecker.acquire();
        Assertions.assertNotNull(tick);
        Assertions.assertNotEquals(-1, tick.getLocation());
        windowChecker.release(tick);
        tick = windowChecker.acquire();
        Assertions.assertNotNull(tick);
        Assertions.assertNotEquals(-1, tick.getLocation());
        windowChecker.release(tick);

        windowChecker.acquire();
        tick = windowChecker.acquire();
        Assertions.assertNotNull(tick);
        Assertions.assertNotEquals(-1, tick.getLocation());
        windowChecker.release(tick);
        windowChecker.acquire();

        Assertions.assertThrows(ThrottlingException.class, () -> {
            windowChecker.acquire();
        });
    }

    @Test
    void acquire() {
        Tick tick = windowChecker.acquire();
        Assertions.assertNotNull(tick);
        Assertions.assertNotEquals(-1, tick.getLocation());
        tick = windowChecker.acquire();
        Assertions.assertNotNull(tick);
        Assertions.assertNotEquals(-1, tick.getLocation());

        Assertions.assertThrows(ThrottlingException.class, () -> {
            windowChecker.acquire();
        });
    }
}