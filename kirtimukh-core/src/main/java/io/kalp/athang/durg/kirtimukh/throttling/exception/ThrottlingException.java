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

package io.kalp.athang.durg.kirtimukh.throttling.exception;

import io.kalp.athang.durg.kirtimukh.throttling.enums.ThrottlingWindowUnit;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ThrottlingException extends RuntimeException {
    private final ThrottlingWindowUnit unit;

    private final long window;

    private final int cardinality;

    private final int threshold;

    private final String message;
}