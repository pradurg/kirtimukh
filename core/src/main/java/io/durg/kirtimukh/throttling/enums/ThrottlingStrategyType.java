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

package io.durg.kirtimukh.throttling.enums;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
public enum ThrottlingStrategyType {
    QUOTA() {
        @Override
        public <T> T accept(ThrottlingStrategyTypeVisitor<T> visitor) {
            return visitor.visitQuota();
        }
    },
    LEAKY_BUCKET {
        @Override
        public <T> T accept(ThrottlingStrategyTypeVisitor<T> visitor) {
            return visitor.visitLeakyBucket();
        }
    },
    PRIORITY_BUCKET {
        @Override
        public <T> T accept(ThrottlingStrategyTypeVisitor<T> visitor) {
            return visitor.visitPriorityBuckets();
        }
    },
    NG {
        @Override
        public <T> T accept(ThrottlingStrategyTypeVisitor<T> visitor) {
            return visitor.visitNg();
        }
    };

    public abstract <T> T accept(ThrottlingStrategyType.ThrottlingStrategyTypeVisitor<T> visitor);

    public interface ThrottlingStrategyTypeVisitor<T> {
        public T visitQuota();

        public T visitLeakyBucket();

        public T visitPriorityBuckets();

        public T visitNg();
    }
}