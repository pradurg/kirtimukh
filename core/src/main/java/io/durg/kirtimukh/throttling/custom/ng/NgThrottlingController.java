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

import com.google.inject.Singleton;
import io.durg.kirtimukh.throttling.custom.CustomGatePass;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingController;
import io.durg.kirtimukh.throttling.custom.CustomThrottlingStrategyChecker;
import lombok.Builder;

/**
 * Created by pradeep.dalvi on 03/11/20
 */
@Singleton
public class NgThrottlingController extends CustomThrottlingController {
    @Builder
    public NgThrottlingController(final NgThrottlingKeyType.Visitor<CustomGatePass> gatePassVisitor) {
        super(new NgThrottlingKeyResolver(NgThrottlingKeyType.BUCKET, gatePassVisitor));
    }

    @Override
    public CustomThrottlingStrategyChecker checker(final CustomGatePass customGatePass) {
        return new NgStrategyChecker(customGatePass);
    }
}