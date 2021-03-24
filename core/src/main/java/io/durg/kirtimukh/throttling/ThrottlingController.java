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
import io.durg.kirtimukh.throttling.checker.impl.NoStrategyChecker;
import io.durg.kirtimukh.throttling.checker.impl.PriorityBucketStrategyChecker;
import io.durg.kirtimukh.throttling.checker.impl.QuotaStrategyChecker;
import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;
import io.durg.kirtimukh.throttling.custom.GatePass;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.window.WindowChecker;
import io.durg.kirtimukh.throttling.window.WindowCheckerUtils;
import io.durg.kirtimukh.throttling.window.impl.PriorityWindowChecker;
import lombok.Builder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Singleton
public class ThrottlingController {
    private boolean enabled;

    private final NoStrategyChecker noStrategyChecker;

    private final ConcurrentHashMap<String, WindowChecker> windowCheckerMap;
    private final ConcurrentHashMap<String, ThrottlingStrategyType> strategyTypeMap;

    private final CustomThrottlingController customThrottlingController;

    private ThrottlingStrategyConfig defaultStrategyConfig;
    private Map<String, ThrottlingStrategyConfig> commandStrategyConfigs;

    @Builder
    public ThrottlingController(final ThrottlingStrategyConfig defaultConfig,
                                final Map<String, ThrottlingStrategyConfig> commandConfigs,
                                final CustomThrottlingController customThrottlingController) {
        enabled = true;
        noStrategyChecker = new NoStrategyChecker();

        this.windowCheckerMap = new ConcurrentHashMap<>();
        this.strategyTypeMap = new ConcurrentHashMap<>();

        this.defaultStrategyConfig = defaultConfig;
        this.commandStrategyConfigs = commandConfigs;
        this.customThrottlingController = customThrottlingController;

        initialise();
    }

    private boolean initialise() {
        for (Map.Entry<String, ThrottlingStrategyConfig> entry : commandStrategyConfigs.entrySet()) {
            this.strategyTypeMap.put(entry.getKey(), entry.getValue().getType());
        }
        return true;
    }

    private WindowChecker getWindowChecker(final ThrottlingKey throttlingKey) {
        final String configKey = throttlingKey.getConfigName();
        if (!windowCheckerMap.containsKey(configKey)) {
            windowCheckerMap.put(configKey, WindowCheckerUtils.getWindowChecker(throttlingKey, commandStrategyConfigs
                    .getOrDefault(configKey, defaultStrategyConfig)));
        }

        return windowCheckerMap.get(configKey);
    }

    private GatePass getCustomGateKeeper(final ThrottlingKey bucketKey) {
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
                        return new QuotaStrategyChecker(getWindowChecker(bucketKey));
                    }

                    @Override
                    public StrategyChecker visitLeakyBucket() {
                        return new LeakyBucketStrategyChecker(getWindowChecker(bucketKey));
                    }

                    @Override
                    public StrategyChecker visitPriorityBucket() {
                        return new PriorityBucketStrategyChecker((PriorityWindowChecker) getWindowChecker(bucketKey));
                    }

                    @Override
                    public StrategyChecker visitDynamicStrategy() {
                        return null;
                    }

                    @Override
                    public StrategyChecker visitCustomStrategy() {
                        return customThrottlingController.checker(getCustomGateKeeper(bucketKey));
                    }
                });
    }

    public Map<String, WindowChecker> getInfo() {
        return windowCheckerMap;
    }

    public boolean disable() {
        enabled = false;
        return reset();
    }

    public boolean reset() {
        windowCheckerMap.clear();
        strategyTypeMap.clear();

        return initialise();
    }

    public boolean reload(final ThrottlingStrategyConfig defaultConfig,
                          final Map<String, ThrottlingStrategyConfig> commandConfigs) {
        this.defaultStrategyConfig = defaultConfig;
        this.commandStrategyConfigs = commandConfigs;
        return reset();
    }

    public StrategyChecker register(final ThrottlingKey bucketKey) {
        return enabled
                ? getStrategyChecker(bucketKey)
                : noStrategyChecker;
    }
}