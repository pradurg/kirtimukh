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

package io.durg.kirtimukh.dw.throttling;

import io.durg.kirtimukh.throttling.custom.ThrottlingVerdict;
import io.durg.kirtimukh.throttling.exception.impl.CustomThrottlingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CustomThrottlingExceptionMapper implements ExceptionMapper<CustomThrottlingException> {
    @Override
    public Response toResponse(CustomThrottlingException e) {
        return e.getVerdict()
                .accept(new ThrottlingVerdict.Visitor<Response>() {
                    @Override
                    public Response visitAllow() {
                        return null; // Do Nothing
                    }

                    @Override
                    public Response visitDeny() {
                        return Response.status(429)
                                .entity("Too Many Requests")
                                .build();
                    }

                    @Override
                    public Response visitWait() {
                        return Response.status(503)
                                .entity("Service Temporarily Unavailable")
                                .build();
                    }

                    @Override
                    public Response visitAck() {
                        return Response.status(202)
                                .entity("Accepted")
                                .build();
                    }
                });
    }
}
