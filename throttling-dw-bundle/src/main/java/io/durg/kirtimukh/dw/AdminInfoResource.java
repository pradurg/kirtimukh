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

package io.durg.kirtimukh.dw;

import io.durg.aop.annotation.Throttle;
import io.durg.kirtimukh.throttling.ThrottlingManager;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by pradeep.dalvi on 15/10/20
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/kirtimukh/throttling")
public class AdminInfoResource {
    @GET
    @Path("/info")
    public Response list() {
        return Response.ok(ThrottlingManager.getInfo())
                .build();
    }

    @GET
    @Path("/test")
    @Throttle
    public Response test() {
        return Response.ok(ThrottlingManager.getInfo())
                .build();
    }

    @POST
    @Path("/reset")
    @Throttle
    public Response reset() {
        return Response.ok(ThrottlingManager.reset())
                .build();
    }

    @POST
    @Path("/reload")
    @Throttle
    public Response reload() {
        // TODO: Only reset now, introduce config data provider later
        return Response.ok(ThrottlingManager.reset())
                .build();
    }

    @POST
    @Path("/disable")
    @Throttle
    public Response disable() {
        return Response.ok(ThrottlingManager.disable())
                .build();
    }
}