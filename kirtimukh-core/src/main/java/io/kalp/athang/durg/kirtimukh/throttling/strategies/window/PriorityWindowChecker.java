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

package io.kalp.athang.durg.kirtimukh.throttling.strategies.window;

import io.kalp.athang.durg.kirtimukh.throttling.strategies.tick.Tick;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.tick.impl.WindowLocationTick;
import lombok.Builder;

import java.util.Map;

/**
 * Created by pradeep.dalvi on 20/10/20
 */
public class PriorityWindowChecker implements WindowChecker {
    private final String commandName;
    private Map<Integer, Window> priorityWindows;

    @Builder
    public PriorityWindowChecker(final String commandName) {
        this.commandName = commandName;
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
