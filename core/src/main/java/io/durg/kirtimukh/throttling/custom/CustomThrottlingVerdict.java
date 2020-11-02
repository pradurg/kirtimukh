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

/**
 * Created by pradeep.dalvi on 15/10/20
 */
public enum CustomThrottlingVerdict {
    ALLOW() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAllow();
        }
    },
    DENY() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitDeny();
        }
    },
    WAIT() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitWait();
        }
    },
    ACK() {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAck();
        }
    };

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visitAllow();

        T visitDeny();

        T visitWait();

        T visitAck();
    }
}
