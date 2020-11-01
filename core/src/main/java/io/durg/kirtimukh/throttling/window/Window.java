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

package io.durg.kirtimukh.throttling.window;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Data
@Slf4j
public class Window {
    private static final int MIN_BIT_SET_SIZE = 64; // A word

    private final BitSet bitSet;
    private final int threshold;

    private int currentLocation;
    private int cardinality;
    private int maxTicksPerWindow;

    @Builder
    public Window(final int threshold) {
        this.threshold = threshold;

        maxTicksPerWindow = nPower(threshold);
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

    private boolean locate(final int location) {
        if (bitSet.get(location)) {
            return false;
        }

        currentLocation = location;
        cardinality += 1;

        bitSet.set(location);
        return true;
    }

    public int cardinality() {
        return cardinality;
    }

    public synchronized void clear() {
        bitSet.clear();

        currentLocation = 0;
        cardinality = 0;
    }

    public synchronized boolean remove(final int location) {
        bitSet.clear(location);
        cardinality -= 1;
        return true;
    }

    public synchronized int add() {
        int currentRequestCount = bitSet.cardinality();

        // This shouldn't happen. But if it happens, adjust the local counter
        if (currentRequestCount != cardinality) {
            log.warn("Adjusting cardinality from {} to {}", cardinality, currentRequestCount);
            cardinality = currentRequestCount;
        }

        if (currentRequestCount >= threshold) {
            return -1;
        }

        int location;
        do {
            location = bitSet.nextClearBit(currentLocation);
            if (location >= maxTicksPerWindow) {
                currentLocation = 0;
                location = 0;
            }
        } while (!locate(location));
        return location;
    }
}