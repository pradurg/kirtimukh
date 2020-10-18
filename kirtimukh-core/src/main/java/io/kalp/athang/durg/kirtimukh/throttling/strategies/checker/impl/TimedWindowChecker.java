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

import java.util.concurrent.TimeUnit;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TimedWindowChecker extends RequestsWindowChecker {
    private static final long MIN_INACTIVE_WINDOWS_TO_CLEAR = 2;
    private final ThrottlingWindowUnit unit;
    private final long clearAfterInactiveWindows;
    private long prevWindow;
    private long liveWindow;

    @Builder
    public TimedWindowChecker(final String commandName,
                              final ThrottlingWindowUnit unit,
                              final int threshold,
                              final long clearAfterInactiveWindows) {
        super(commandName, threshold);
        this.unit = unit;
        this.liveWindow = getWindow();
        this.clearAfterInactiveWindows = Math.max(clearAfterInactiveWindows, MIN_INACTIVE_WINDOWS_TO_CLEAR);
    }

    private long getWindow() {
        return unit.accept(new ThrottlingWindowUnit.ThrottlingWindowVisitor<Long>() {
            @Override
            public Long visitMillisecond() {
                return System.currentTimeMillis();
            }

            @Override
            public Long visitSecond() {
                return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }

            @Override
            public Long visitMinute() {
                return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
            }
        });
    }

    @Override
    public boolean isChangeInWindow() {
        long window = getWindow();
        if (liveWindow != window) {
            prevWindow = liveWindow;
            liveWindow = window;
            return true;
        }
        return false;
    }

    @Override
    protected boolean isOkayToClear() {
        return ((liveWindow - prevWindow) >= clearAfterInactiveWindows);
    }
}