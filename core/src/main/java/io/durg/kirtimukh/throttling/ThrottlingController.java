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

import com.google.inject.Singleton;
import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.durg.kirtimukh.throttling.ticker.StrategyChecker;
import io.durg.kirtimukh.throttling.ticker.impl.LeakyBucketTicker;
import io.durg.kirtimukh.throttling.ticker.impl.PriorityBucketTicker;
import io.durg.kirtimukh.throttling.ticker.impl.QuotaStrategyTicker;
import io.durg.kirtimukh.throttling.window.impl.PriorityWindowChecker;
import io.durg.kirtimukh.throttling.window.impl.TimedWindowChecker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Singleton
public class ThrottlingController {
    private final ConcurrentHashMap<String, TimedWindowChecker> windowCheckerMap;
    private final ConcurrentHashMap<String, ThrottlingStrategyType> strategyTypeMap;
    private final ThrottlingStrategyConfig defaultStrategyConfig;

    public ThrottlingController(final ThrottlingStrategyConfig defaultConfig,
                                final Map<String, ThrottlingStrategyConfig> commandConfigs) {
        this.windowCheckerMap = new ConcurrentHashMap<>();
        this.strategyTypeMap = new ConcurrentHashMap<>();
        this.defaultStrategyConfig = defaultConfig;

        for (Map.Entry<String, ThrottlingStrategyConfig> entry : commandConfigs.entrySet()) {
            this.strategyTypeMap.put(entry.getKey(), entry.getValue().getType());
            this.windowCheckerMap.put(entry.getKey(), getWindowChecker(entry.getKey(), entry.getValue()));
        }
    }

    private TimedWindowChecker getTimedWindowChecker(final String configKey,
                                                     final ThrottlingStrategyConfig strategyConfig) {
        return TimedWindowChecker.builder()
                .commandKey(configKey)
                .strategyConfig(strategyConfig)
                .build();
    }

    private TimedWindowChecker getWindowChecker(final String configKey,
                                                final ThrottlingStrategyConfig strategyConfig) {
        return strategyConfig.getUnit()
                .accept(new ThrottlingWindowUnit.ThrottlingWindowVisitor<TimedWindowChecker>() {
                    @Override
                    public TimedWindowChecker visitMillisecond() {
                        return getTimedWindowChecker(configKey, strategyConfig);
                    }

                    @Override
                    public TimedWindowChecker visitSecond() {
                        return getTimedWindowChecker(configKey, strategyConfig);
                    }

                    @Override
                    public TimedWindowChecker visitMinute() {
                        return getTimedWindowChecker(configKey, strategyConfig);
                    }
                });
    }

    private TimedWindowChecker getWindowChecker(final ThrottlingBucketKey bucketKey) {
        final String configKey = bucketKey.getConfigName();
        if (!windowCheckerMap.containsKey(configKey)) {
            windowCheckerMap.put(configKey, getWindowChecker(configKey, defaultStrategyConfig));
        }

        return windowCheckerMap.get(configKey);
    }

    private PriorityWindowChecker getPriorityWindowChecker(final ThrottlingBucketKey bucketKey) {
        return PriorityWindowChecker.builder()
                .bucketKey(bucketKey)
                .build();
    }

    private StrategyChecker getStrategyChecker(final ThrottlingBucketKey bucketKey) {
        final String configKey = bucketKey.getConfigName();

        ThrottlingStrategyType strategyType = strategyTypeMap.getOrDefault(configKey,
                defaultStrategyConfig.getType());

        return strategyType
                .accept(new ThrottlingStrategyType.ThrottlingStrategyTypeVisitor<StrategyChecker>() {
                    @Override
                    public StrategyChecker visitQuota() {
                        TimedWindowChecker windowChecker = getWindowChecker(bucketKey);

                        return new QuotaStrategyTicker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitLeakyBucket() {
                        TimedWindowChecker windowChecker = getWindowChecker(bucketKey);

                        return new LeakyBucketTicker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitPriorityBuckets() {
                        PriorityWindowChecker windowChecker = getPriorityWindowChecker(bucketKey);

                        return new PriorityBucketTicker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitNg() {
                        throw new UnsupportedOperationException("Ng strategy unsupported");
                    }
                });
    }

    public Map<String, TimedWindowChecker> getInfo() {
        return windowCheckerMap;
    }

    public StrategyChecker register(final ThrottlingBucketKey bucketKey) {
        return getStrategyChecker(bucketKey);
    }
}