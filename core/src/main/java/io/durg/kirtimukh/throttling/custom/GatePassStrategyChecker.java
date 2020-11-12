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

import io.durg.kirtimukh.throttling.checker.StrategyChecker;

/**
 * Created by pradeep.dalvi on 02/11/20
 */
public abstract class GatePassStrategyChecker<T> implements StrategyChecker {
    private final GatePass<T> gatePass;

    public GatePassStrategyChecker(final GatePass<T> gatePass) {
        this.gatePass = gatePass;
    }

    @Override
    public void enter() {
        ThrottlingVerdict verdict = gatePass.enter();

        if (verdict != ThrottlingVerdict.ALLOW) {
            react(verdict, gatePass);
        }
    }

    @Override
    public void exit() {
        gatePass.exit();
    }

    public abstract void react(final ThrottlingVerdict verdict, final GatePass<T> gatePass);
}