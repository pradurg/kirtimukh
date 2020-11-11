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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jackson.Jackson;
import io.durg.kirtimukh.throttling.ThrottlingKey;
import io.durg.kirtimukh.throttling.ThrottlingManager;
import io.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * Created by pradeep.dalvi on 25/10/20
 */
class InfoResourceTest {

    @BeforeEach
    void setUp() {
        ThrottlingManager.initialise(LeakyBucketThrottlingStrategyConfig.builder()
                        .threshold(1)
                        .build(),
                new HashMap<>(),
                null,
                e -> new UnsupportedOperationException(),
                null);
    }

    @Test
    void list() {
        ThrottlingManager.register(ThrottlingKey.builder()
                .clazz(InfoResourceTest.class)
                .functionName("list")
                .build());
        Assertions.assertNotNull(ThrottlingManager.getInfo());
    }

    @Test
    void serialise() throws JsonProcessingException {
        ThrottlingManager.register(ThrottlingKey.builder()
                .clazz(InfoResourceTest.class)
                .functionName("list")
                .build());

        Assertions.assertNotNull(Jackson.newObjectMapper()
                .writeValueAsString(ThrottlingManager.getInfo()));
    }

    @Test
    void test() {
        ThrottlingManager.register(ThrottlingKey.builder()
                .clazz(InfoResourceTest.class)
                .functionName("test")
                .build());
        Assertions.assertNotNull(ThrottlingManager.getInfo());
    }
}