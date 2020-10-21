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

import com.google.inject.Singleton;
import io.kalp.athang.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.StrategyChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.impl.LeakyBucketTicker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.impl.PriorityBucketTicker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.impl.QuotaStrategyTicker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.window.PriorityWindowChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.window.TimedWindowChecker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Singleton
public class ThrottlingController {
    private final Map<String, TimedWindowChecker> windowCheckerMap;
    private final Map<String, ThrottlingStrategyType> strategyTypeMap;
    private final ThrottlingStrategyConfig defaultStrategyConfig;

    public ThrottlingController(final ThrottlingStrategyConfig defaultConfig,
                                final Map<String, ThrottlingStrategyConfig> commandConfigs) {
        this.windowCheckerMap = new HashMap<>();
        this.strategyTypeMap = new HashMap<>();
        this.defaultStrategyConfig = defaultConfig;

        for (Map.Entry<String, ThrottlingStrategyConfig> entry : commandConfigs.entrySet()) {
            this.strategyTypeMap.put(entry.getKey(), entry.getValue().getType());
            this.windowCheckerMap.put(entry.getKey(), getWindowChecker(entry.getKey(), entry.getValue()));
        }
    }

    private TimedWindowChecker getTimedWindowChecker(final String commandName,
                                                     final ThrottlingStrategyConfig strategyConfig) {
        return TimedWindowChecker.builder()
                .commandName(commandName)
                .strategyConfig(strategyConfig)
                .build();
    }

    private TimedWindowChecker getWindowChecker(final String commandName,
                                                final ThrottlingStrategyConfig strategyConfig) {
        return strategyConfig.getUnit()
                .accept(new ThrottlingWindowUnit.ThrottlingWindowVisitor<TimedWindowChecker>() {
                    @Override
                    public TimedWindowChecker visitMillisecond() {
                        return getTimedWindowChecker(commandName, strategyConfig);
                    }

                    @Override
                    public TimedWindowChecker visitSecond() {
                        return getTimedWindowChecker(commandName, strategyConfig);
                    }

                    @Override
                    public TimedWindowChecker visitMinute() {
                        return getTimedWindowChecker(commandName, strategyConfig);
                    }
                });
    }

    private synchronized TimedWindowChecker getWindowChecker(final String commandName) {
        if (!windowCheckerMap.containsKey(commandName)) {
            windowCheckerMap.put(commandName, getWindowChecker(commandName, defaultStrategyConfig));
        }

        return windowCheckerMap.get(commandName);
    }

    private PriorityWindowChecker getPriorityWindowChecker(final String commandName) {
        return PriorityWindowChecker.builder()
                .commandName(commandName)
                .build();
    }

    private StrategyChecker getStrategyChecker(final String commandName) {
        ThrottlingStrategyType strategyType = strategyTypeMap.getOrDefault(commandName,
                defaultStrategyConfig.getType());

        return strategyType
                .accept(new ThrottlingStrategyType.ThrottlingStrategyTypeVisitor<StrategyChecker>() {
                    @Override
                    public StrategyChecker visitQuota() {
                        TimedWindowChecker windowChecker = getWindowChecker(commandName);

                        return new QuotaStrategyTicker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitLeakyBucket() {
                        TimedWindowChecker windowChecker = getWindowChecker(commandName);

                        return new LeakyBucketTicker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitPriorityBuckets() {
                        PriorityWindowChecker windowChecker = getPriorityWindowChecker(commandName);

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

    public StrategyChecker register(final String rateLimitedFunctionName) {
        return getStrategyChecker(rateLimitedFunctionName);
    }
}