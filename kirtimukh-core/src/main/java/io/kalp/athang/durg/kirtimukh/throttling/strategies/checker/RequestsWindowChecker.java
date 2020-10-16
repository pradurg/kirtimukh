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

import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import io.kalp.athang.durg.kirtimukh.throttling.exception.ThrottlingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.BitSet;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Slf4j
public abstract class RequestsWindowChecker {
    private static final int MIN_BIT_SET_SIZE = 128;
    private final ThrottlingWindowUnit unit;
    private final int maxTicksPerWindow;
    @Getter
    private final int threshold;
    private final SecureRandom random;
    private final BitSet bitSet;
    private long currentWindow;

    protected RequestsWindowChecker(final ThrottlingWindowUnit unit,
                                    final int threshold) {
        this.unit = unit;
        this.currentWindow = 0;
        this.maxTicksPerWindow = nPower(threshold);
        this.threshold = threshold;

        this.bitSet = new BitSet(maxTicksPerWindow);
        this.random = new SecureRandom(Long.toBinaryString(System.currentTimeMillis()).getBytes());
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

    public abstract long getCurrentWindow();

    protected abstract boolean isOkayToClear();

    private boolean locate(final int location) {
        long window = getCurrentWindow();
        if (currentWindow != window) {
            currentWindow = window;
            if (isOkayToClear()) {
                bitSet.clear();
            }
        }

        if (bitSet.get(location)) {
            return false;
        }

        bitSet.set(location);
        log.debug("Set at location: " + location + " cardinality: " + cardinality());
        return true;
    }

    public synchronized boolean release(final int location) {
        bitSet.clear(location);
        return true;
    }

    public synchronized int cardinality() {
        return bitSet.cardinality();
    }

    public synchronized int acquire() {
        if (bitSet.cardinality() >= threshold) {
            log.warn("Cardinality " + cardinality() + " is about to exceed allowed limit " + threshold);
            throw ThrottlingException.builder()
                    .unit(unit)
                    .window(getCurrentWindow())
                    .cardinality(bitSet.cardinality())
                    .threshold(threshold)
                    .message("Thank you contacting us! :-)")
                    .build();
        }
        log.info("Cardinality " + cardinality() + " allowed limit " + threshold);

        int marker;
        do {
            marker = random.nextInt(maxTicksPerWindow);
        } while (!locate(marker));
        return marker;
    }
}