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

package io.durg.kirtimukh.throttling.exception.impl;

import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class QuotaThrottlingException extends ThrottlingException {
    private final String commandKey;

    private final ThrottlingWindowUnit unit;

    private final int cardinality;

    private final int threshold;

    @Builder
    public QuotaThrottlingException(final String commandKey,
                                    final ThrottlingWindowUnit unit,
                                    final int cardinality,
                                    final int threshold,
                                    final String message) {
        super(ThrottlingStrategyType.QUOTA, message, false);
        this.commandKey = commandKey;
        this.unit = unit;
        this.cardinality = cardinality;
        this.threshold = threshold;
    }
}