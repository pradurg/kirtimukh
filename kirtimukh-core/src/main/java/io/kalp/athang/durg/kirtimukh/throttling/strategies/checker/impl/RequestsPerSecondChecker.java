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

package io.kalp.athang.durg.kirtimukh.throttling.strategies.checker.impl;

import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.checker.RequestsWindowChecker;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RequestsPerSecondChecker extends RequestsWindowChecker {
    @Builder
    public RequestsPerSecondChecker(final int threshold) {
        super(ThrottlingWindowUnit.SECOND, threshold);
    }

    @Override
    public long getCurrentWindow() {
        long mills = System.currentTimeMillis();
        return (mills - (mills % 1000)) / 1000;
    }

    @Override
    protected boolean isOkayToClear() {
        int cardinality = cardinality();
        return (cardinality / getThreshold()) * 100 >= 10;
    }
}