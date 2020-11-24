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

package io.durg.kirtimukh.throttling;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pradeep.dalvi on 24/10/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThrottlingKey {
    private String bucketName;

    private Class<?> clazz;

    private String functionName;

    private boolean optional;

    public String getClassName() {
        return clazz.getSimpleName();
    }

    public String getCommandName() {
        return getClassName() + "." + functionName;
    }

    public String getConfigName() {
        return Strings.isNullOrEmpty(bucketName)
                ? getCommandName()
                : bucketName;
    }
}