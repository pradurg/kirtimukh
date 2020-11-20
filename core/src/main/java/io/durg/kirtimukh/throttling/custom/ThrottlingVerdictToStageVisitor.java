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

package io.durg.kirtimukh.throttling.custom;

import io.durg.kirtimukh.throttling.enums.ThrottlingStage;

/**
 * Created by pradeep.dalvi on 20/11/20
 */
public class ThrottlingVerdictToStageVisitor {
    private static final ThrottlingVerdict.Visitor<ThrottlingStage> visitor
            = new ThrottlingVerdict.Visitor<ThrottlingStage>() {
        @Override
        public ThrottlingStage visitAllow() {
            return null; // Shouldn't happen
        }

        @Override
        public ThrottlingStage visitDeny() {
            return ThrottlingStage.DENIED;
        }

        @Override
        public ThrottlingStage visitWait() {
            return ThrottlingStage.WAIT;
        }

        @Override
        public ThrottlingStage visitAck() {
            return ThrottlingStage.ACKNOWLEDGED;
        }
    };

    public static ThrottlingStage fromVerdict(ThrottlingVerdict verdict) {
        return verdict.accept(visitor);
    }
}
