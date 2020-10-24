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

package io.kalp.athang.durg.kirtimukh.throttling.tick.impl;

import io.kalp.athang.durg.kirtimukh.throttling.tick.Tick;
import lombok.Builder;
import lombok.Getter;

/**
 * Created by pradeep.dalvi on 20/10/20
 */
public class WindowLocationTick extends Tick {
    @Getter
    private final int windowId;

    @Builder
    public WindowLocationTick(final int windowId,
                              final int location) {
        super(location);
        this.windowId = windowId;
    }
}
