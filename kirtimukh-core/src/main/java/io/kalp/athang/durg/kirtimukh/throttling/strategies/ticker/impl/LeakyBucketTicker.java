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

package io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.impl;

import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.tick.Tick;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.ticker.StrategyChecker;
import io.kalp.athang.durg.kirtimukh.throttling.strategies.window.TimedWindowChecker;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
public class LeakyBucketTicker implements StrategyChecker {
    private final TimedWindowChecker windowChecker;
    private Tick tick;

    public LeakyBucketTicker(TimedWindowChecker windowChecker) {
        this.windowChecker = windowChecker;
    }

    @Override
    public void enter() throws ThrottlingException {
        tick = windowChecker.acquire();
    }

    @Override
    public void exit() {
        windowChecker.release(tick);
    }
}