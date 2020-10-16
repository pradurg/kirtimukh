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
import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle;
import io.kalp.athang.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.LeakyBucketTicker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.QuotaStrategyTicker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.StrategyChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.checker.RequestsWindowChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.checker.impl.RequestsPerMillisChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.checker.impl.RequestsPerSecondChecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Singleton
public class ThrottlingController {
    private final Map<String, RequestsWindowChecker> windowCheckerMap;
    private final Map<String, ThrottlingStrategyType> strategyTypeMap;
    private final ThrottlingStrategyConfig defaultStrategyConfig;

    public ThrottlingController(final ThrottlingStrategyConfig defaultConfig,
                                final List<ThrottlingStrategyConfig> configs) {
        this.windowCheckerMap = new HashMap<>();
        this.strategyTypeMap = new HashMap<>();
        this.defaultStrategyConfig = defaultConfig;

        for (ThrottlingStrategyConfig config : configs) {
            this.strategyTypeMap.put(config.getName(), config.getType());
            this.windowCheckerMap.put(config.getName(), getWindowChecker(config));
        }
    }

    private RequestsWindowChecker getWindowChecker(final ThrottlingStrategyConfig strategyConfig) {
        if (strategyConfig.getUnit() == ThrottlingWindowUnit.MILLISECOND) {
            return RequestsPerMillisChecker.builder()
                    .threshold(strategyConfig.getThreshold())
                    .build();
        }

        return RequestsPerSecondChecker.builder()
                .threshold(strategyConfig.getThreshold())
                .build();
    }

    private synchronized RequestsWindowChecker getWindowChecker(final String commandName) {
        if (!windowCheckerMap.containsKey(commandName)) {
            windowCheckerMap.put(commandName, getWindowChecker(defaultStrategyConfig));
        }

        return windowCheckerMap.get(commandName);
    }

    private StrategyChecker getStrategyChecker(final String commandName) {
        RequestsWindowChecker windowChecker = getWindowChecker(commandName);

        ThrottlingStrategyType strategyType = strategyTypeMap.getOrDefault(commandName,
                defaultStrategyConfig.getType());
        if (strategyType == ThrottlingStrategyType.QUOTA) {
            return new QuotaStrategyTicker(windowChecker);
        }

        return new LeakyBucketTicker(windowChecker);
    }

    public Map<String, RequestsWindowChecker> getInfo() {
        return windowCheckerMap;
    }

    public StrategyChecker register(final Throttle throttle) {
        return getStrategyChecker(throttle.name());
    }

    public StrategyChecker register(final String rateLimitedFunctionName) {
        return getStrategyChecker(rateLimitedFunctionName);
    }
}