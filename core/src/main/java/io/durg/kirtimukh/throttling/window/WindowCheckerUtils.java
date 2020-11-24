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

package io.durg.kirtimukh.throttling.window;

import io.durg.kirtimukh.throttling.ThrottlingKey;
import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.config.impl.PriorityBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.config.impl.QuotaThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.window.impl.LeakyBucketWindowChecker;
import io.durg.kirtimukh.throttling.window.impl.PriorityWindowChecker;
import io.durg.kirtimukh.throttling.window.impl.QuotaWindowChecker;
import lombok.experimental.UtilityClass;

/**
 * Created by pradeep.dalvi on 12/11/20
 */
@UtilityClass
public class WindowCheckerUtils {
    private QuotaWindowChecker getTimedWindowChecker(final ThrottlingKey throttlingKey,
                                                     final QuotaThrottlingStrategyConfig strategyConfig) {
        return QuotaWindowChecker.builder()
                .commandKey(throttlingKey.getConfigName())
                .strategyConfig(strategyConfig)
                .build();
    }

    private LeakyBucketWindowChecker getSimpleWindowChecker(final ThrottlingKey throttlingKey,
                                                            final ThrottlingStrategyConfig strategyConfig) {
        return LeakyBucketWindowChecker.builder()
                .commandKey(throttlingKey.getConfigName())
                .strategyConfig(strategyConfig)
                .build();
    }

    private PriorityWindowChecker getPriorityWindowChecker(final ThrottlingKey throttlingKey,
                                                           final PriorityBucketThrottlingStrategyConfig strategyConfig) {
        return PriorityWindowChecker.builder()
                .bucketKey(throttlingKey)
                .strategyConfig(strategyConfig)
                .build();
    }

    public WindowChecker getWindowChecker(final ThrottlingKey throttlingKey,
                                          final ThrottlingStrategyConfig strategyConfig) {
        return strategyConfig.getType()
                .accept(new ThrottlingStrategyType.Visitor<WindowChecker>() {
                    @Override
                    public WindowChecker visitQuota() {
                        return getTimedWindowChecker(throttlingKey, (QuotaThrottlingStrategyConfig) strategyConfig);
                    }

                    @Override
                    public WindowChecker visitLeakyBucket() {
                        return getSimpleWindowChecker(throttlingKey, strategyConfig);
                    }

                    @Override
                    public WindowChecker visitPriorityBucket() {
                        return getPriorityWindowChecker(throttlingKey, (PriorityBucketThrottlingStrategyConfig) strategyConfig);
                    }

                    @Override
                    public WindowChecker visitDynamicStrategy() {
                        return null;
                    }

                    @Override
                    public WindowChecker visitCustomStrategy() {
                        return null;
                    }
                });
    }
}
