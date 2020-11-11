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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import io.durg.kirtimukh.throttling.checker.StrategyChecker;
import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;
import io.durg.kirtimukh.throttling.enums.ThrottlingStage;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.durg.kirtimukh.throttling.window.WindowChecker;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@UtilityClass
public class ThrottlingManager {
    private ThrottlingController controller;

    private ThrottlingExceptionTranslator<? extends RuntimeException> translator;

    private MetricRegistry metrics;

    private static final String PREFIX = "kirtimukh";
    private static final String BUCKETS = "buckets";
    private static final String COMMANDS = "commands";
    private static final String SEPARATOR = ".";

    public void initialise(final ThrottlingStrategyConfig defaultConfig,
                           final Map<String, ThrottlingStrategyConfig> commandConfigs,
                           final CustomThrottlingController customThrottlingController,
                           final ThrottlingExceptionTranslator<? extends RuntimeException> exceptionTranslator,
                           final MetricRegistry metricRegistry) {
        controller = ThrottlingController.builder()
                .defaultConfig(defaultConfig)
                .commandConfigs(commandConfigs)
                .customThrottlingController(customThrottlingController)
                .build();
        translator = exceptionTranslator;
        metrics = metricRegistry;
    }

    public Map<String, WindowChecker> getInfo() {
        return controller.getInfo();
    }

    public StrategyChecker register(final ThrottlingKey bucketKey) {
        return controller.register(bucketKey);
    }

    public void translate(final ThrottlingException t) {
        ThrottlingExceptionTranslator.translate(translator, t);
    }

    private void timer(final String name, final ThrottlingStage stage, final Stopwatch stopwatch) {
        Timer timer = metrics.timer(String.join(SEPARATOR, PREFIX, name, stage.getName()));
        if (timer != null) {
            timer.update(stopwatch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }

    public void ticker(final ThrottlingKey bucketKey,
                       final ThrottlingStage stage,
                       final Stopwatch stopwatch) {
        if (metrics == null) {
            return;
        }

        if (!Strings.isNullOrEmpty(bucketKey.getBucketName())) {
            timer(BUCKETS + SEPARATOR + bucketKey.getBucketName(), stage, stopwatch);
        }
        timer(COMMANDS + SEPARATOR + bucketKey.getCommandName(), stage, stopwatch);
    }

    public boolean disable() {
        return controller.reset();
    }

    public boolean reset() {
        return controller.reset();
    }

    public boolean reload(final ThrottlingStrategyConfig defaultConfig,
                          final Map<String, ThrottlingStrategyConfig> commandConfigs) {
        return controller.reload(defaultConfig, commandConfigs);
    }
}