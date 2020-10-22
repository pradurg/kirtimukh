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

package io.kalp.athang.dw.bundle;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingController;
import io.kalp.athang.durg.kirtimukh.throttling.ThrottlingManager;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingExceptionTranslator;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
public abstract class ThrottlingBundle<T extends Configuration> implements ConfiguredBundle<T> {
    private ThrottlingController controller;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        ThrottlingBundleConfiguration throttlingBundleConfiguration = getThrottlingConfiguration(configuration);
        ThrottlingExceptionTranslator translator = getExceptionTranslator();

        ThrottlingManager.initialise(throttlingBundleConfiguration.getDefaultStrategyConfig(),
                throttlingBundleConfiguration.getCommandStrategyConfigs(), translator, environment.metrics());

        environment.jersey()
                .register(new InfoResource());
    }

    protected abstract ThrottlingBundleConfiguration getThrottlingConfiguration(T configuration);

    protected abstract <E extends RuntimeException> ThrottlingExceptionTranslator<E> getExceptionTranslator();
}