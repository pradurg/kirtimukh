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

package io.durg.kirtimukh.dw;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.durg.kirtimukh.dw.throttling.AdminInfoResource;
import io.durg.kirtimukh.dw.throttling.CustomThrottlingExceptionMapper;
import io.durg.kirtimukh.dw.throttling.ThrottlingExceptionMapper;
import io.durg.kirtimukh.throttling.ThrottlingExceptionTranslator;
import io.durg.kirtimukh.throttling.ThrottlingManager;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;

import java.util.Objects;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
public abstract class ThrottlingBundle<T extends Configuration, K> implements ConfiguredBundle<T> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Nothing happens here
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        ThrottlingBundleConfiguration throttlingBundleConfiguration = getThrottlingConfiguration(configuration);

        ThrottlingManager.initialise(throttlingBundleConfiguration.getDefaultStrategyConfig(),
                throttlingBundleConfiguration.getCommandStrategyConfigs(),
                getCustomController(),
                getExceptionTranslator(),
                environment.metrics());

        environment.jersey()
                .register(new AdminInfoResource());

        if (Objects.isNull(getExceptionTranslator())) {
            environment.jersey()
                    .register(Objects.nonNull(getCustomController())
                            ? new CustomThrottlingExceptionMapper()
                            : new ThrottlingExceptionMapper());
        }
    }

    protected abstract ThrottlingBundleConfiguration getThrottlingConfiguration(T configuration);

    protected abstract <E extends RuntimeException> ThrottlingExceptionTranslator<E> getExceptionTranslator();

    protected abstract CustomThrottlingController<K> getCustomController();
}