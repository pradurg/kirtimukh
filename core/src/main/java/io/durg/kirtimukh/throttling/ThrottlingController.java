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
import io.durg.kirtimukh.throttling.checker.StrategyChecker;
import io.durg.kirtimukh.throttling.checker.impl.LeakyBucketStrategyChecker;
import io.durg.kirtimukh.throttling.checker.impl.PriorityBucketStrategyChecker;
import io.durg.kirtimukh.throttling.checker.impl.QuotaStrategyChecker;
import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.custom.CustomGatePass;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.durg.kirtimukh.throttling.window.impl.PriorityWindowChecker;
import io.durg.kirtimukh.throttling.window.impl.TimedWindowChecker;
import lombok.Builder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Singleton
public class ThrottlingController {
    private final ConcurrentHashMap<String, TimedWindowChecker> windowCheckerMap;
    private final ConcurrentHashMap<String, ThrottlingStrategyType> strategyTypeMap;
    private final ThrottlingStrategyConfig defaultStrategyConfig;
    private final CustomThrottlingController customThrottlingController;

    @Builder
    public ThrottlingController(final ThrottlingStrategyConfig defaultConfig,
                                final Map<String, ThrottlingStrategyConfig> commandConfigs,
                                final CustomThrottlingController customThrottlingController) {
        this.windowCheckerMap = new ConcurrentHashMap<>();
        this.strategyTypeMap = new ConcurrentHashMap<>();
        this.defaultStrategyConfig = defaultConfig;
        this.customThrottlingController = customThrottlingController;

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
                .accept(new ThrottlingWindowUnit.Visitor<TimedWindowChecker>() {
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

    private TimedWindowChecker getWindowChecker(final ThrottlingKey bucketKey) {
        final String configKey = bucketKey.getConfigName();
        if (!windowCheckerMap.containsKey(configKey)) {
            windowCheckerMap.put(configKey, getWindowChecker(configKey, defaultStrategyConfig));
        }

        return windowCheckerMap.get(configKey);
    }

    private PriorityWindowChecker getPriorityWindowChecker(final ThrottlingKey bucketKey) {
        return PriorityWindowChecker.builder()
                .bucketKey(bucketKey)
                .build();
    }

    private CustomGatePass getCustomGateKeeper(final ThrottlingKey bucketKey) {
        if (Objects.isNull(customThrottlingController)) {
            throw new UnsupportedOperationException("Custom config found without resolver");
        }
        return customThrottlingController.resolve(bucketKey);
    }

    private StrategyChecker getStrategyChecker(final ThrottlingKey bucketKey) {
        final String configKey = bucketKey.getConfigName();

        ThrottlingStrategyType strategyType = strategyTypeMap.getOrDefault(configKey,
                defaultStrategyConfig.getType());

        return strategyType
                .accept(new ThrottlingStrategyType.Visitor<StrategyChecker>() {
                    @Override
                    public StrategyChecker visitQuota() {
                        TimedWindowChecker windowChecker = getWindowChecker(bucketKey);

                        return new QuotaStrategyChecker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitLeakyBucket() {
                        TimedWindowChecker windowChecker = getWindowChecker(bucketKey);

                        return new LeakyBucketStrategyChecker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitPriorityBucket() {
                        PriorityWindowChecker windowChecker = getPriorityWindowChecker(bucketKey);

                        return new PriorityBucketStrategyChecker(windowChecker);
                    }

                    @Override
                    public StrategyChecker visitCustomStrategy() {
                        CustomGatePass customGatePass = getCustomGateKeeper(bucketKey);

                        return customThrottlingController.checker(customGatePass);
                    }
                });
    }

    public Map<String, TimedWindowChecker> getInfo() {
        return windowCheckerMap;
    }

    public StrategyChecker register(final ThrottlingKey bucketKey) {
        return getStrategyChecker(bucketKey);
    }
}