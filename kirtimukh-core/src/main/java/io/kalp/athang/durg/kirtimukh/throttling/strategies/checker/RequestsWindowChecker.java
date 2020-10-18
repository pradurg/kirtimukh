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

package io.kalp.athang.durg.kirtimukh.throttling.strategies.checker;

import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Slf4j
public abstract class RequestsWindowChecker {
    private static final int MIN_BIT_SET_SIZE = 64; // A word
    private final String commandName;
    private final BitSet bitSet;
    private final int threshold;
    private int currentLocation;

    protected RequestsWindowChecker(final String commandName,
                                    final int threshold) {
        this.commandName = commandName;
        this.threshold = threshold;

        int maxTicksPerWindow = nPower(threshold);
        this.bitSet = new BitSet(maxTicksPerWindow);
    }

    private static int nPower(int number) {
        if ((number < 0) ||
                (number == Integer.MAX_VALUE)) {
            return Integer.MAX_VALUE;
        }
        int n = 1;
        do {
            n = n << 1;
        } while (number >= n);

        return Math.max(n, MIN_BIT_SET_SIZE);
    }

    protected abstract boolean isChangeInWindow();

    protected abstract boolean isOkayToClear();

    private boolean locate(final int location) {
        if (isChangeInWindow()) {
            currentLocation = 0;
            if (isOkayToClear()) {
                log.debug("[{}] Clearing bitset", commandName);
                bitSet.clear();
            }
        }

        if (bitSet.get(location)) {
            return false;
        }

        bitSet.set(location);
        log.debug("[{}] Set at location: {} cardinality: {}", commandName, location, bitSet.cardinality());
        return true;
    }

    public synchronized boolean release(final int location) {
        bitSet.clear(location);
        return true;
    }

    public synchronized int acquire() {
        int cardinality = bitSet.cardinality();
        if (cardinality >= threshold) {
            log.warn("[{}] Cardinality {} exceeding allowed limit {}", commandName, cardinality, threshold);
            throw ThrottlingException.builder()
                    .cardinality(cardinality)
                    .threshold(threshold)
                    .message("Thank you contacting us! :-)")
                    .build();
        }
        log.debug("[{}] Cardinality {} allowed limit {}", commandName, cardinality, threshold);

        int marker;
        do {
            marker = bitSet.nextClearBit(currentLocation);
        } while (!locate(marker));
        return marker;
    }
}