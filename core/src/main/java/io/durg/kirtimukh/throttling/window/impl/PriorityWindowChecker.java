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

package io.durg.kirtimukh.throttling.window.impl;

import io.durg.kirtimukh.throttling.ThrottlingKey;
import io.durg.kirtimukh.throttling.config.impl.PriorityBucketThrottlingStrategyConfig;
import io.durg.kirtimukh.throttling.tick.Tick;
import io.durg.kirtimukh.throttling.tick.impl.WindowLocationTick;
import io.durg.kirtimukh.throttling.window.WindowChecker;
import lombok.Builder;

/**
 * Created by pradeep.dalvi on 20/10/20
 */
public class PriorityWindowChecker implements WindowChecker {
    @Builder
    public PriorityWindowChecker(final ThrottlingKey bucketKey,
                                 final PriorityBucketThrottlingStrategyConfig strategyConfig) {
    }

    @Override
    public WindowLocationTick acquire() {
        return WindowLocationTick.builder()
                .windowId(1)
                .location(1)
                .build();
    }

    @Override
    public boolean release(Tick location) {
        return true;
    }
}
