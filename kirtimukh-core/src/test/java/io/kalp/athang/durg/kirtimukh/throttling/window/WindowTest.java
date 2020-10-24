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

package io.kalp.athang.durg.kirtimukh.throttling.window;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by pradeep.dalvi on 21/10/20
 */
class WindowTest {

    private Window window;

    @BeforeEach
    void setUpBefore() {
        window = Window.builder()
                .threshold(2)
                .build();
    }

    @AfterEach
    void tearDown() {
        window.clear();
    }

    @Test
    void cardinality() {
        window.add();
        Assertions.assertEquals(1, window.cardinality());
        window.add();
        Assertions.assertEquals(2, window.cardinality());
        Assertions.assertEquals(-1, window.add());
        Assertions.assertEquals(2, window.cardinality());
        System.out.println(window);
    }

    @Test
    void clear() {
        window.add();
        Assertions.assertEquals(1, window.cardinality());
        window.add();
        Assertions.assertEquals(2, window.cardinality());
        window.clear();
        Assertions.assertEquals(0, window.cardinality());
    }

    @Test
    void remove() {
        window.add();
        Assertions.assertEquals(1, window.cardinality());
        int loc = window.add();
        Assertions.assertEquals(2, window.cardinality());
        window.remove(loc);
        Assertions.assertEquals(1, window.cardinality());
        loc = window.add();
        Assertions.assertEquals(-1, window.add());
        Assertions.assertEquals(2, window.cardinality());
    }

    @Test
    void add() {
        window.add();
        Assertions.assertEquals(1, window.cardinality());
        int loc = window.add();
        Assertions.assertEquals(2, window.cardinality());
        Assertions.assertEquals(-1, window.add());
        window.remove(loc);
        Assertions.assertEquals(1, window.cardinality());
        System.out.println(window);
    }
}