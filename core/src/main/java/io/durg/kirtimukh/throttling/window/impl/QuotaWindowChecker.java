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

import io.durg.kirtimukh.throttling.config.impl.QuotaThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.durg.kirtimukh.throttling.exception.impl.QuotaThrottlingException;
import io.durg.kirtimukh.throttling.tick.Tick;
import io.durg.kirtimukh.throttling.tick.impl.LocationTick;
import io.durg.kirtimukh.throttling.window.Window;
import io.durg.kirtimukh.throttling.window.WindowChecker;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Data
@Slf4j
public class QuotaWindowChecker implements WindowChecker {
    private final String commandKey;
    private final ThrottlingWindowUnit unit;

    private final long clearAfterInactiveWindows;
    private long prevWindow;
    private long liveWindow;

    private final Window window;

    @Builder
    public QuotaWindowChecker(final String commandKey,
                              final QuotaThrottlingStrategyConfig strategyConfig) {
        this.commandKey = commandKey;
        this.unit = strategyConfig.getUnit();

        this.liveWindow = getWindow();
        this.clearAfterInactiveWindows = strategyConfig.getWindows();
        this.window = Window.builder()
                .threshold(strategyConfig.getThreshold())
                .build();
    }

    private long getWindow() {
        return unit.accept(new ThrottlingWindowUnit.Visitor<Long>() {
            @Override
            public Long visitMillisecond() {
                return System.currentTimeMillis();
            }

            @Override
            public Long visitSecond() {
                return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }

            @Override
            public Long visitMinute() {
                return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
            }
        });
    }

    private boolean isChangeInWindow() {
        long currentWindow = getWindow();
        if (liveWindow != currentWindow) {
            prevWindow = liveWindow;
            liveWindow = currentWindow;
            return true;
        }
        return false;
    }

    private boolean isOkayToClear() {
        return ((liveWindow - prevWindow) >= clearAfterInactiveWindows);
    }

    private void precheck() {
        if (isChangeInWindow() && isOkayToClear()) {
            log.debug("[{}] Clearing bitset", commandKey);
            window.clear();
        }
    }

    @Override
    public synchronized boolean release(final Tick location) {
        return window.remove(location.getLocation());
    }

    @Override
    public synchronized Tick acquire() {
        precheck();

        int location = window.add();
        if (location < 0) {
            throw QuotaThrottlingException.builder()
                    .commandKey(commandKey)
                    .cardinality(window.cardinality())
                    .unit(unit)
                    .threshold(window.getThreshold())
                    .message("Threshold limits exhausted")
                    .build();
        }
        log.debug("[{}] Cardinality {} allowed limit {}", commandKey, window.cardinality(), window.getThreshold());

        return LocationTick.builder()
                .location(location)
                .build();
    }
}