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

package io.kalp.athang.durg.kirtimukh.throttling.window.impl;

import io.kalp.athang.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.QuotaThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.kalp.athang.durg.kirtimukh.throttling.tick.Tick;
import io.kalp.athang.durg.kirtimukh.throttling.tick.impl.LocationTick;
import io.kalp.athang.durg.kirtimukh.throttling.window.Window;
import io.kalp.athang.durg.kirtimukh.throttling.window.WindowChecker;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Slf4j
public class TimedWindowChecker implements WindowChecker {
    private static final long MIN_INACTIVE_WINDOWS_TO_CLEAR = 5;

    private final String commandKey;
    private final ThrottlingStrategyType strategyType;
    private final ThrottlingWindowUnit unit;
    private final int threshold;

    private final long clearAfterInactiveWindows;
    private long prevWindow;
    private long liveWindow;

    private final Window window;

    @Builder
    public TimedWindowChecker(final String commandKey,
                              final ThrottlingStrategyConfig strategyConfig) {
        this.commandKey = commandKey;
        this.strategyType = strategyConfig.getType();
        this.unit = strategyConfig.getUnit();
        this.threshold = strategyConfig.getThreshold();

        this.liveWindow = getWindow();
        this.clearAfterInactiveWindows = strategyConfig.getType()
                .accept(new ThrottlingStrategyType.ThrottlingStrategyTypeVisitor<Long>() {
                    @Override
                    public Long visitQuota() {
                        return ((QuotaThrottlingStrategyConfig) strategyConfig).getWindows();
                    }

                    @Override
                    public Long visitLeakyBucket() {
                        return MIN_INACTIVE_WINDOWS_TO_CLEAR;
                    }

                    @Override
                    public Long visitPriorityBuckets() {
                        return MIN_INACTIVE_WINDOWS_TO_CLEAR;
                    }

                    @Override
                    public Long visitNg() {
                        return null;
                    }
                });

        this.window = Window.builder()
                .threshold(threshold)
                .build();
    }

    private long getWindow() {
        return unit.accept(new ThrottlingWindowUnit.ThrottlingWindowVisitor<Long>() {
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
    public synchronized boolean release(final Tick tick) {
        return window.remove(tick.getLocation());
    }

    @Override
    public synchronized Tick acquire() {
        precheck();

        int location = window.add();
        if (location < 0) {
            throw ThrottlingException.builder()
                    .commandKey(commandKey)
                    .strategyType(strategyType)
                    .cardinality(window.cardinality())
                    .unit(unit)
                    .threshold(threshold)
                    .message("Threshold limits exhausted")
                    .build();
        }
        log.debug("[{}] Cardinality {} allowed limit {}", commandKey, window.cardinality(), threshold);

        return LocationTick.builder()
                .location(location)
                .build();
    }
}