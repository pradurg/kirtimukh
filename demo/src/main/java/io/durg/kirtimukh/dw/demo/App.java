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
import io.durg.kirtimukh.throttling.ThrottlingKey;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;
import io.durg.kirtimukh.throttling.custom.GatePass;
import io.durg.kirtimukh.throttling.custom.GatePassStrategyChecker;
import io.durg.kirtimukh.throttling.custom.ThrottlingKeyResolver;
import io.durg.kirtimukh.throttling.custom.ThrottlingKeyType;
import io.durg.kirtimukh.throttling.custom.ThrottlingVerdict;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.exception.impl.CustomThrottlingException;

import java.util.Random;

/**
 * Created by pradeep.dalvi on 12/11/20
 */
public class App extends Application<AppConfig> {
    @Override
    public void initialize(final Bootstrap<AppConfig> bootstrap) {
        bootstrap.addBundle(new ThrottlingBundle<AppConfig, ThrottlingKeyType>() {
            @Override
            protected ThrottlingBundleConfiguration getThrottlingConfiguration(AppConfig appConfig) {
                return appConfig.getThrottlingConfig();
            }

            @Override
            protected CustomThrottlingController<ThrottlingKeyType> getCustomController() {
                return new CustomThrottlingController<ThrottlingKeyType>(
                        new ThrottlingKeyResolver<ThrottlingKeyType>() {

                            // Random evaluator for demo purpose
                            final Evaluator evaluator = new Evaluator() {
                                final Random random = new Random();

                                @Override
                                public ThrottlingVerdict evaluate(String key) {
                                    switch (random.nextInt(4)) {
                                        case 0:
                                            return ThrottlingVerdict.DENY;
                                        case 1:
                                            return ThrottlingVerdict.WAIT;
                                        case 2:
                                            return ThrottlingVerdict.ACK;
                                        default:
                                            return ThrottlingVerdict.ALLOW;
                                    }
                                }
                            };

                            @Override
                            public GatePass<ThrottlingKeyType> resolve(ThrottlingKey bucketKey) {
                                return new GatePass<ThrottlingKeyType>(ThrottlingKeyType.COMMAND,
                                        bucketKey.getConfigName()) {

                                    // Custom entrance logic goes here
                                    @Override
                                    public ThrottlingVerdict enter() {
                                        return evaluator.evaluate(getKey());
                                    }

                                    @Override
                                    public long retryAfter() {
                                        return 0;
                                    }

                                    // Custom exit logic goes here
                                    @Override
                                    public void exit() {

                                    }
                                };
                            }
                        }
                ) {
                    @Override
                    public GatePassStrategyChecker<ThrottlingKeyType> checker(GatePass<ThrottlingKeyType> gatePass) {
                        return new GatePassStrategyChecker<ThrottlingKeyType>(gatePass) {
                            @Override
                            public void react(ThrottlingVerdict verdict, GatePass<ThrottlingKeyType> gatePass) {
                                // Define separate actions e.g. raising separate exceptions for all verdicts
                                verdict.accept(new ThrottlingVerdict.Visitor<Void>() {
                                    @Override
                                    public Void visitAllow() {
                                        return null;
                                    }

                                    @Override
                                    public Void visitDeny() {
                                        throw CustomThrottlingException.builder()
                                                .keyType(gatePass.getKeyType())
                                                .key(gatePass.getKey())
                                                .keyType(gatePass.getKeyType())
                                                .verdict(ThrottlingVerdict.DENY)
                                                .message("Threshold limits exhausted")
                                                .build();
                                    }

                                    @Override
                                    public Void visitWait() {
                                        throw CustomThrottlingException.builder()
                                                .keyType(gatePass.getKeyType())
                                                .key(gatePass.getKey())
                                                .verdict(ThrottlingVerdict.WAIT)
                                                .retryAfterMs(gatePass.retryAfter())
                                                .graceful(true)
                                                .message(String.format("Limits exhausted so wait for %s",
                                                        gatePass.retryAfter()))
                                                .build();
                                    }

                                    @Override
                                    public Void visitAck() {
                                        throw CustomThrottlingException.builder()
                                                .keyType(gatePass.getKeyType())
                                                .key(gatePass.getKey())
                                                .verdict(ThrottlingVerdict.ACK)
                                                .graceful(true)
                                                .message("Limits exhausted but request accepted")
                                                .build();
                                    }
                                });
                            }
                        };
                    }
                };
            }

            @Override
            protected ThrottlingExceptionTranslator<AppException> getExceptionTranslator() {
                return e -> e.getStrategyType()
                        .accept(new ThrottlingStrategyType.Visitor<AppException>() {
                            @Override
                            public AppException visitQuota() {
                                return AppException.builder()
                                        .responseCode(ResponseCode.LIMIT_EXCEEDED)
                                        .message("Limit Exceeded")
                                        .build();
                            }

                            @Override
                            public AppException visitLeakyBucket() {
                                return AppException.builder()
                                        .responseCode(ResponseCode.TOO_MANY_REQUESTS)
                                        .message("Too Many Requests")
                                        .build();
                            }

                            @Override
                            public AppException visitPriorityBucket() {
                                return AppException.builder()
                                        .responseCode(ResponseCode.TEMPORARILY_UNAVAILABLE)
                                        .message("Temporarily Unavailable")
                                        .build();
                            }

                            @Override
                            public AppException visitDynamicStrategy() {
                                return null;
                            }

                            @Override
                            public AppException visitCustomStrategy() {
                                return ((CustomThrottlingException) e)
                                        .getVerdict()
                                        .accept(new ThrottlingVerdict.Visitor<AppException>() {
                                            @Override
                                            public AppException visitAllow() {
                                                // Do nothing
                                                return null;
                                            }

                                            @Override
                                            public AppException visitDeny() {
                                                return AppException.builder()
                                                        .responseCode(ResponseCode.TEMPORARILY_UNAVAILABLE)
                                                        .message("Temporarily Unavailable")
                                                        .build();
                                            }

                                            @Override
                                            public AppException visitWait() {
                                                return AppException.builder()
                                                        .responseCode(ResponseCode.TOO_MANY_REQUESTS)
                                                        .message("Too Many Requests")
                                                        .build();
                                            }

                                            @Override
                                            public AppException visitAck() {
                                                return AppException.builder()
                                                        .responseCode(ResponseCode.ACCEPTED)
                                                        .message("Request Accepted")
                                                        .build();
                                            }
                                        });
                            }
                        });
            }
        });
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) throws Exception {

    }
}
