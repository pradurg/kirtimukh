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

import io.durg.kirtimukh.throttling.custom.CustomGatePass;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingVerdict;
import io.durg.kirtimukh.throttling.enums.ThrottlingStrategyType;
import lombok.Builder;

/**
 * Created by pradeep.dalvi on 03/11/20
 */
public class NgThrottlingVerdictVisitor implements CustomThrottlingVerdict.Visitor<Void> {
    private final CustomGatePass customGatePass;

    @Builder
    public NgThrottlingVerdictVisitor(final CustomGatePass customGatePass) {
        this.customGatePass = customGatePass;
    }

    @Override
    public Void visitAllow() {
        return null;
    }

    @Override
    public Void visitDeny() {
        throw NgThrottlingException.builder()
                .strategyType(ThrottlingStrategyType.CUSTOM_STRATEGY)
                .keyType(customGatePass.getKeyType())
                .key(customGatePass.getKey())
                .keyType(customGatePass.getKeyType())
                .verdict(CustomThrottlingVerdict.DENY)
                .message("Threshold limits exhausted")
                .build();
    }

    @Override
    public Void visitWait() {
        throw NgThrottlingException.builder()
                .strategyType(ThrottlingStrategyType.CUSTOM_STRATEGY)
                .keyType(customGatePass.getKeyType())
                .key(customGatePass.getKey())
                .verdict(CustomThrottlingVerdict.WAIT)
                .retryAfterMs(customGatePass.retryAfter())
                .graceful(true)
                .message(String.format("Limits exhausted so wait for %s", customGatePass.retryAfter()))
                .build();
    }

    @Override
    public Void visitAck() {
        throw NgThrottlingException.builder()
                .strategyType(ThrottlingStrategyType.CUSTOM_STRATEGY)
                .keyType(customGatePass.getKeyType())
                .key(customGatePass.getKey())
                .verdict(CustomThrottlingVerdict.ACK)
                .graceful(true)
                .message("Limits exhausted but request accepted")
                .build();
    }
}
