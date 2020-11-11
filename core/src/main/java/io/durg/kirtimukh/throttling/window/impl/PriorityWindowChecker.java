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

import io.durg.kirtimukh.throttling.ThrottlingKey;
import io.durg.kirtimukh.throttling.config.PriorityBucketThrottlingConfig;
import io.durg.kirtimukh.throttling.config.impl.PriorityBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.exception.SimpleThrottlingException;
import io.durg.kirtimukh.throttling.tick.Tick;
import io.durg.kirtimukh.throttling.tick.impl.WindowLocationTick;
import io.durg.kirtimukh.throttling.window.Window;
import io.durg.kirtimukh.throttling.window.WindowChecker;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by pradeep.dalvi on 20/10/20
 */
@Data
@Slf4j
public class PriorityWindowChecker implements WindowChecker {
    private int priority;
    private ThrottlingKey throttlingKey;
    private ConcurrentNavigableMap<Integer, Window> priorityWindows;

    @Builder
    public PriorityWindowChecker(final ThrottlingKey bucketKey,
                                 final PriorityBucketThrottlingStrategyConfig strategyConfig) {
        this.throttlingKey = bucketKey;
        this.priorityWindows = new ConcurrentSkipListMap<>();

        for (Map.Entry<String, PriorityBucketThrottlingConfig> configEntry :
                strategyConfig.getBucketConfigs()
                        .entrySet()) {
            if (!this.priorityWindows.containsKey(configEntry.getValue().getPriority())) {
                this.priorityWindows.put(configEntry.getValue()
                                .getPriority(),
                        Window.builder()
                                .threshold(strategyConfig.getSharedBucketThreshold())
                                .build());
            }
        }

        this.priorityWindows.put(Integer.MAX_VALUE, Window.builder()
                .threshold(strategyConfig.getSharedBucketThreshold())
                .build());
    }

    @Override
    public WindowLocationTick acquire() {
        for (Map.Entry<Integer, Window> entry :
                priorityWindows.tailMap(priority).entrySet()) {
            int location = entry.getValue()
                    .add();
            if (location >= 0) {
                return WindowLocationTick.builder()
                        .windowId(entry.getKey())
                        .location(location)
                        .build();
            }
        }

        throw SimpleThrottlingException.builder()
                .commandKey(throttlingKey.getConfigName())
                .strategyType(ThrottlingStrategyType.PRIORITY_BUCKET)
                .message("Threshold limits exhausted")
                .build();
    }

    @Override
    public boolean release(Tick location) {
        WindowLocationTick windowLocationTick = (WindowLocationTick) location;
        return priorityWindows.get(windowLocationTick.getWindowId())
                .remove(windowLocationTick.getLocation());
    }
}
