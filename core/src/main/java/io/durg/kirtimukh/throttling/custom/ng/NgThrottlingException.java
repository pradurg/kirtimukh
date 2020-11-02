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

package io.durg.kirtimukh.throttling.custom.ng;

import io.durg.kirtimukh.throttling.custom.CustomThrottlingVerdict;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import io.durg.kirtimukh.throttling.exception.ThrottlingException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Created by pradeep.dalvi on 02/11/20
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class NgThrottlingException extends ThrottlingException {
    private final Enum keyType;

    private final String key;

    private final CustomThrottlingVerdict verdict;

    private final long retryAfterMs;

    @Builder
    public NgThrottlingException(final ThrottlingStrategyType strategyType,
                                 final Enum keyType,
                                 final String key,
                                 final boolean graceful,
                                 final CustomThrottlingVerdict verdict,
                                 final long retryAfterMs,
                                 final String message) {
        super(strategyType, message, graceful);
        this.keyType = keyType;
        this.key = key;
        this.verdict = verdict;
        this.retryAfterMs = retryAfterMs;
    }
}
