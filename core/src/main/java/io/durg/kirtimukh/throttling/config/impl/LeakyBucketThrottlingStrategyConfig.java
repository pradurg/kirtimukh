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

package io.durg.kirtimukh.throttling.config.impl;

import io.durg.kirtimukh.throttling.config.ThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by pradeep.dalvi on 21/10/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeakyBucketThrottlingStrategyConfig extends ThrottlingStrategyConfig {
    public LeakyBucketThrottlingStrategyConfig() {
        super(ThrottlingStrategyType.LEAKY_BUCKET);
    }

    @Builder
    public LeakyBucketThrottlingStrategyConfig(final ThrottlingWindowUnit unit,
                                               final int threshold) {
        super(ThrottlingStrategyType.LEAKY_BUCKET, unit, threshold);
    }
}