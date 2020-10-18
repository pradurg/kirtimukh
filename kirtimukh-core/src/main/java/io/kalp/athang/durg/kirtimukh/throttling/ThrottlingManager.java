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

import io.kalp.athang.durg.kirtimukh.throttling.annotation.Throttle;
import io.kalp.athang.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingExceptionTranslator;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.StrategyChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.checker.RequestsWindowChecker;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@UtilityClass
public class ThrottlingManager {
    private ThrottlingController controller;

    @Getter
    private ThrottlingExceptionTranslator translator;

    public void initialise(final ThrottlingStrategyConfig defaultConfig,
                           final List<ThrottlingStrategyConfig> configs,
                           final ThrottlingExceptionTranslator exceptionTranslator) {
        controller = new ThrottlingController(defaultConfig, configs);
        translator = exceptionTranslator;
    }

    public Map<String, RequestsWindowChecker> getInfo() {
        return controller.getInfo();
    }

    public StrategyChecker register(final Throttle throttle) {
        return controller.register(throttle.name());
    }

    public StrategyChecker register(final String rateLimitedFunctionName) {
        return controller.register(rateLimitedFunctionName);
    }
}
