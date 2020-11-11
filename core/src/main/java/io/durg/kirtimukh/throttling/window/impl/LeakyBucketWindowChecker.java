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

import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.exception.impl.LeakyBucketThrottlingException;
import io.durg.kirtimukh.throttling.tick.Tick;
import io.durg.kirtimukh.throttling.tick.impl.LocationTick;
import io.durg.kirtimukh.throttling.window.Window;
import io.durg.kirtimukh.throttling.window.WindowChecker;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by pradeep.dalvi on 11/11/20
 */
@Data
@Slf4j
public class LeakyBucketWindowChecker implements WindowChecker {
    private final String commandKey;

    private final Window window;

    @Builder
    public LeakyBucketWindowChecker(final String commandKey,
                                    final ThrottlingStrategyConfig strategyConfig) {
        this.commandKey = commandKey;

        this.window = Window.builder()
                .threshold(strategyConfig.getThreshold())
                .build();
    }

    @Override
    public Tick acquire() {
        int location = window.add();
        if (location < 0) {
            throw LeakyBucketThrottlingException.builder()
                    .commandKey(commandKey)
                    .cardinality(window.cardinality())
                    .threshold(window.getThreshold())
                    .message("Threshold limits exhausted")
                    .build();
        }
        log.debug("[{}] Cardinality {} allowed limit {}", commandKey, window.cardinality(), window.getThreshold());

        return LocationTick.builder()
                .location(location)
                .build();
    }

    @Override
    public boolean release(Tick location) {
        return window.remove(location.getLocation());
    }
}
