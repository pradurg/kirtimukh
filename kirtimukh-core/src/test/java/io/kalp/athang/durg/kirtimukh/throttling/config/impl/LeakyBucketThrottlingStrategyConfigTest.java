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

package io.kalp.athang.durg.kirtimukh.throttling.config.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by pradeep.dalvi on 23/10/20
 */
class LeakyBucketThrottlingStrategyConfigTest {
    LeakyBucketThrottlingStrategyConfig config;

    @BeforeEach
    void setUp() {
        config = LeakyBucketThrottlingStrategyConfig.builder()
                .unit(ThrottlingWindowUnit.SECOND)
                .threshold(10)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSerDe() {
        ObjectMapper mapper = Jackson.newObjectMapper();

        Assertions.assertDoesNotThrow(() -> {
            String configStr = mapper.writeValueAsString(config);
            Assertions.assertEquals(config, mapper.readValue(configStr, LeakyBucketThrottlingStrategyConfig.class));
        });
    }
}