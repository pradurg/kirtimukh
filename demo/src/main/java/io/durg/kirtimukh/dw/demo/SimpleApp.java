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

package io.durg.kirtimukh.dw.demo;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.durg.kirtimukh.dw.ThrottlingBundle;
import io.durg.kirtimukh.dw.ThrottlingBundleConfiguration;
import io.durg.kirtimukh.throttling.ThrottlingExceptionTranslator;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;

/**
 * Created by pradeep.dalvi on 12/11/20
 */
public class SimpleApp extends Application<AppConfig> {
    @Override
    public void initialize(final Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(new ThrottlingBundle<AppConfig>() {
            @Override
            protected ThrottlingBundleConfiguration getThrottlingConfiguration(AppConfig appConfig) {
                return appConfig.getThrottlingConfig();
            }

            @Override
            protected CustomThrottlingController getCustomController() {
                return null;
            }

            @Override
            protected ThrottlingExceptionTranslator<AppException> getExceptionTranslator() {
                return e -> AppException.builder()
                        .responseCode(ResponseCode.TOO_MANY_REQUESTS)
                        .message("Too Many Requests")
                        .build();
            }
        });
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) throws Exception {

    }
}
