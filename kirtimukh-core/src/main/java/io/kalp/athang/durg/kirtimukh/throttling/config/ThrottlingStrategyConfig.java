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

package io.kalp.athang.durg.kirtimukh.throttling.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.LeakyBucketThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.PriorityBucketThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.config.impl.QuotaThrottlingStrategyConfig;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import lombok.Data;
import lombok.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "QUOTA", value = QuotaThrottlingStrategyConfig.class),
        @JsonSubTypes.Type(name = "LEAKY_BUCKET", value = LeakyBucketThrottlingStrategyConfig.class),
        @JsonSubTypes.Type(name = "PRIORITY_BUCKET", value = PriorityBucketThrottlingStrategyConfig.class),
})
public abstract class ThrottlingStrategyConfig {
    @NonNull
    private ThrottlingStrategyType type;

    @NonNull
    private ThrottlingWindowUnit unit;

    @Valid
    @Min(1)
    private int threshold;

    protected ThrottlingStrategyConfig(final ThrottlingStrategyType type) {
        this.type = type;
        this.unit = ThrottlingWindowUnit.SECOND;
    }

    protected ThrottlingStrategyConfig(final ThrottlingStrategyType type,
                                       final ThrottlingWindowUnit unit,
                                       final int threshold) {
        this.type = type;
        this.unit = unit;
        this.threshold = threshold;
    }
}