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

package io.durg.kirtimukh.throttling.custom.ng;

import io.durg.kirtimukh.throttling.custom.GatePass;
import io.durg.kirtimukh.throttling.custom.ThrottlingKeyType;
import io.durg.kirtimukh.throttling.custom.ThrottlingVerdict;
import io.durg.kirtimukh.throttling.exception.impl.CustomThrottlingException;
import lombok.Builder;

/**
 * Created by pradeep.dalvi on 03/11/20
 */
public class NgThrottlingVerdictVisitor implements ThrottlingVerdict.Visitor<Void> {
    private final GatePass<ThrottlingKeyType> gatePass;

    @Builder
    public NgThrottlingVerdictVisitor(final GatePass<ThrottlingKeyType> gatePass) {
        this.gatePass = gatePass;
    }

    @Override
    public Void visitAllow() {
        return null;
    }

    @Override
    public Void visitDeny() {
        throw CustomThrottlingException.builder()
                .keyType(gatePass.getKeyType())
                .key(gatePass.getKey())
                .keyType(gatePass.getKeyType())
                .verdict(ThrottlingVerdict.DENY)
                .message("Threshold limits exhausted")
                .build();
    }

    @Override
    public Void visitWait() {
        throw CustomThrottlingException.builder()
                .keyType(gatePass.getKeyType())
                .key(gatePass.getKey())
                .verdict(ThrottlingVerdict.WAIT)
                .retryAfterMs(gatePass.retryAfter())
                .graceful(true)
                .message(String.format("Limits exhausted so wait for %s", gatePass.retryAfter()))
                .build();
    }

    @Override
    public Void visitAck() {
        throw CustomThrottlingException.builder()
                .keyType(gatePass.getKeyType())
                .key(gatePass.getKey())
                .verdict(ThrottlingVerdict.ACK)
                .graceful(true)
                .message("Limits exhausted but request accepted")
                .build();
    }
}